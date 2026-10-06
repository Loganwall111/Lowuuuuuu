package dev.siftbridge.client;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * The frame ring lives in a named shared-memory mapping — RAM only, nothing touches disk, and it
 * dies with the publisher unless {@link #release()} unlinks it (the ESC path does both).
 *
 * <ul>
 * <li>Windows: pagefile-backed {@code CreateFileMappingW}, opened by name from the host.</li>
 * <li>Linux (sandbox/harness): the identical layout in a tmpfs file under {@code /dev/shm},
 *     mapped read/write, so the same reader code works on both platforms.</li>
 * </ul>
 */
final class SharedMemory {
	private static final int PAGE_READWRITE = 0x04;
	private static final int FILE_MAP_ALL_ACCESS = 0xF001F;

	final MemorySegment segment;
	private final FileChannel channel;     // Linux path; null on Windows
	private final Path file;               // Linux path; null on Windows

	private SharedMemory(final MemorySegment segment, final FileChannel channel, final Path file) {
		this.segment = segment;
		this.channel = channel;
		this.file = file;
	}

	static SharedMemory create(final String windowsName, final long size) throws Throwable {
		if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
			return createWindows(windowsName, size);
		}
		return createPosix(windowsName, size);
	}

	private static SharedMemory createWindows(final String name, final long size) throws Throwable {
		Linker linker = Linker.nativeLinker();
		SymbolLookup kernel32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
		MethodHandle createFileMapping = linker.downcallHandle(
			kernel32.find("CreateFileMappingW").orElseThrow(),
			FunctionDescriptor.of(
				ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
			)
		);
		MethodHandle mapViewOfFile = linker.downcallHandle(
			kernel32.find("MapViewOfFile").orElseThrow(),
			FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG)
		);
		MemorySegment wideName = Arena.global().allocateFrom(ValueLayout.JAVA_BYTE, (name + "\0").getBytes(StandardCharsets.UTF_16LE));
		MemorySegment invalidHandle = MemorySegment.ofAddress(-1L);
		MemorySegment handle = (MemorySegment)createFileMapping.invoke(
			invalidHandle, MemorySegment.NULL, PAGE_READWRITE, (int)(size >>> 32), (int)size, wideName
		);
		if (handle.address() == 0L) {
			throw new IllegalStateException("CreateFileMappingW failed for " + name);
		}

		MemorySegment view = (MemorySegment)mapViewOfFile.invoke(handle, FILE_MAP_ALL_ACCESS, 0, 0, size);
		if (view.address() == 0L) {
			throw new IllegalStateException("MapViewOfFile failed for " + name);
		}

		return new SharedMemory(view.reinterpret(size), null, null);
	}

	private static SharedMemory createPosix(final String windowsName, final long size) throws Throwable {
		Path path = Path.of("/dev/shm", windowsName.replaceFirst("^Local\\\\", "").replace('\\', '_'));
		try (FileChannel ch = FileChannel.open(path,
				java.util.Set.of(java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE))) {
			ch.truncate(size);
			MemorySegment view = ch.map(FileChannel.MapMode.READ_WRITE, 0, size, Arena.global());
			view.byteOrder().order(ByteOrder.LITTLE_ENDIAN);
			return new SharedMemory(view, ch, path);
		}
	}

	/** Zero the mapping and, on Linux, unlink the tmpfs file. Safe to call twice. */
	void release() {
		try {
			VarHandle.releaseFence();
			segment.fill((byte)0);
		} catch (RuntimeException ignored) {
			// process may already be tearing the mapping down
		}
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException ignored) {
			}
		}
		if (file != null) {
			try {
				java.nio.file.Files.deleteIfExists(file);
			} catch (IOException ignored) {
			}
		}
	}
}
