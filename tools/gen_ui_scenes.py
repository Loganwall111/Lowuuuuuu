#!/usr/bin/env python3
import os
SC = "scenes"
def w(path, body):
    full = os.path.join(SC, path); os.makedirs(os.path.dirname(full), exist_ok=True)
    open(full, "w").write(body)
def node(name, type_, parent="", script=None, props=None):
    lines = [f'[node name="{name}" type="{type_}"']
    if parent != "": lines.append(f'parent="{parent}"')
    lines.append("]")
    if script: lines.append(f'script = ExtResource("{script}")')
    if props:
        for k,v in props.items(): lines.append(f'{k} = {v}')
    return "\n".join(lines) + "\n"
HEAD = '[gd_scene load_steps={steps} format=3]\n\n'

# ---------- SpeciesCard ----------
def gen_card():
    exts=[("Script","res://scripts/ui/species_card.gd")]
    n=""
    n+=node("SpeciesCard","PanelContainer","","1",{"theme_override_constants":{"minimum_size":"Vector2(220, 280)"} })
    n+=node("VBox","VBoxContainer","SpeciesCard")
    n+=node("Name","Label","VBox",props={"text":"Species","theme_override_font_sizes":{"font_size":"22"}})
    n+=node("Category","Label","VBox",props={"text":"Category","autowrap_mode":"2"})
    n+=node("Environment","Label","VBox",props={"text":"World","autowrap_mode":"2"})
    n+=node("Movement","Label","VBox",props={"text":"Movement","autowrap_mode":"2"})
    n+=node("Focus","Label","VBox",props={"text":"Focus","autowrap_mode":"2"})
    n+=node("AccentBar","ColorRect","VBox",props={"color":"Color(0.36,0.95,0.82,1)","custom_minimum_size":"Vector2(0, 4)"})
    w("ui/SpeciesCard.tscn", HEAD.format(steps=2+len(exts)) + ''.join(f'[ext_resource type="Script" path="{e}" id="{i+1}"]\n' for i,e in enumerate([x[1] for x in exts])) + "\n" + n)

# ---------- MainMenu ----------
def gen_menu():
    card="res://scenes/ui/SpeciesCard.tscn"
    exts=[("Script","res://scripts/ui/main_menu_controller.gd"),("PackedScene",card)]
    n=""
    n+=node("MainMenu","Control","","1",{"mouse_filter":"2"})
    n+=node("Background","ColorRect","MainMenu",props={"color":"Color(0.02,0.04,0.07,1)","anchors_preset":"15","anchor_right":"1","anchor_bottom":"1"})
    n+=node("VBox","VBoxContainer","MainMenu",props={"anchors_preset":"15","anchor_right":"1","anchor_bottom":"1","offset_left":"40","offset_top":"40","offset_right":"-40","offset_bottom":"-40","theme_override_constants":{"separation":"18"}})
    n+=node("Title","Label","VBox",props={"text":"AEON","theme_override_font_sizes":{"font_size":"64"}})
    n+=node("Subtitle","Label","VBox",props={"text":"Begin as a spark of life.","autowrap_mode":"2"})
    n+=node("CardContainer","HBoxContainer","VBox",props={"theme_override_constants":{"separation":"24"},"alignment":"1","size_flags_vertical":"3"})
    n+=node("SettingsButton","Button","VBox",props={"text":"Settings"})
    n+=node("QuitButton","Button","VBox",props={"text":"Quit"})
    w("menu/MainMenu.tscn", HEAD.format(steps=2+len(exts)) +
      '[ext_resource type="Script" path="res://scripts/ui/main_menu_controller.gd" id="1"]\n'
      '[ext_resource type="PackedScene" path="%s" id="2"]\n\n' % card + n)

# ---------- HUD ----------
def gen_hud():
    exts=[("Script","res://scripts/ui/hud.gd")]
    n=""
    n+=node("HUD","CanvasLayer","","1",{"layer":"2"})
    n+=node("Vitals","VBoxContainer","HUD",props={"offset_left":"24","offset_top":"24","custom_minimum_size":"Vector2(260,0)","theme_override_constants":{"separation":"8"}})
    n+=node("EnergyBar","ProgressBar","Vitals",props={"max_value":"100","value":"100","show_percentage":"false","custom_minimum_size":"Vector2(240,10)"})
    n+=node("HealthBar","ProgressBar","Vitals",props={"max_value":"100","value":"100","show_percentage":"false","custom_minimum_size":"Vector2(240,10)"})
    n+=node("MassLabel","Label","Vitals",props={"text":"Biomass: 0.00"})
    n+=node("EvolutionBar","ProgressBar","Vitals",props={"max_value":"1","value":"0","show_percentage":"false","custom_minimum_size":"Vector2(240,10)"})
    n+=node("Status","VBoxContainer","HUD",props={"offset_left":"24","offset_top":"200","theme_override_constants":{"separation":"6"}})
    n+=node("StageLabel","Label","Status",props={"text":"Stage: -"})
    n+=node("ModeLabel","Label","Status",props={"text":"Mode: Swimming"})
    n+=node("DepthLabel","Label","Status",props={"text":"Surface"})
    n+=node("Toast","Label","HUD",props={"offset_left":"24","offset_top":"-60","anchor_top":"1","anchor_bottom":"1","offset_bottom":"-24","autowrap_mode":"2","modulate":"Color(0.86,0.95,0.94,0)"})
    w("ui/HUD.tscn", HEAD.format(steps=2+len(exts)) + '[ext_resource type="Script" path="res://scripts/ui/hud.gd" id="1"]\n\n' + n)

# ---------- EvolutionPanel ----------
def gen_evo():
    exts=[("Script","res://scripts/ui/evolution_panel.gd")]
    n=""
    n+=node("EvolutionPanel","Control","","1",{"mouse_filter":"2"})
    n+=node("Backdrop","ColorRect","EvolutionPanel",props={"color":"Color(0,0,0,0.5)","anchors_preset":"15","anchor_right":"1","anchor_bottom":"1"})
    n+=node("Panel","PanelContainer","EvolutionPanel",props={"anchors_preset":"8","anchor_left":"0.5","anchor_top":"0.5","anchor_right":"0.5","anchor_bottom":"0.5","offset_left":"-260","offset_top":"-220","offset_right":"260","offset_bottom":"220"})
    n+=node("VBox","VBoxContainer","Panel",props={"theme_override_constants":{"separation":"12"}})
    n+=node("Title","Label","VBox",props={"text":"Evolution Available","horizontal_alignment":"1","theme_override_font_sizes":{"font_size":"26"}})
    n+=node("Options","VBoxContainer","VBox",props={"theme_override_constants":{"separation":"8"}})
    n+=node("CloseButton","Button","VBox",props={"text":"Close"})
    w("ui/EvolutionPanel.tscn", HEAD.format(steps=2+len(exts)) + '[ext_resource type="Script" path="res://scripts/ui/evolution_panel.gd" id="1"]\n\n' + n)

# ---------- PauseMenu ----------
def gen_pause():
    exts=[("Script","res://scripts/ui/pause_menu.gd")]
    n=""
    n+=node("PauseMenu","Control","","1",{"mouse_filter":"2"})
    n+=node("Backdrop","ColorRect","PauseMenu",props={"color":"Color(0,0,0,0.55)","anchors_preset":"15","anchor_right":"1","anchor_bottom":"1"})
    n+=node("Panel","PanelContainer","PauseMenu",props={"anchors_preset":"8","anchor_left":"0.5","anchor_top":"0.5","anchor_right":"0.5","anchor_bottom":"0.5","offset_left":"-180","offset_top":"-160","offset_right":"180","offset_bottom":"160"})
    n+=node("VBox","VBoxContainer","Panel",props={"theme_override_constants":{"separation":"14"}})
    n+=node("Title","Label","VBox",props={"text":"Paused","horizontal_alignment":"1","theme_override_font_sizes":{"font_size":"30"}})
    n+=node("ResumeButton","Button","VBox",props={"text":"Resume"})
    n+=node("SaveButton","Button","VBox",props={"text":"Save Game"})
    n+=node("QuitButton","Button","VBox",props={"text":"Quit to Menu"})
    w("ui/PauseMenu.tscn", HEAD.format(steps=2+len(exts)) + '[ext_resource type="Script" path="res://scripts/ui/pause_menu.gd" id="1"]\n\n' + n)

# ---------- DebugOverlay ----------
def gen_debug():
    exts=[("Script","res://scripts/ui/debug_overlay.gd")]
    n=""
    n+=node("DebugOverlay","Control","","1",{"mouse_filter":"2"})
    n+=node("Log","Label","DebugOverlay",props={"offset_left":"12","offset_top":"12","custom_minimum_size":"Vector2(420,360)","autowrap_mode":"2"})
    w("ui/DebugOverlay.tscn", HEAD.format(steps=2+len(exts)) + '[ext_resource type="Script" path="res://scripts/ui/debug_overlay.gd" id="1"]\n\n' + n)

# ---------- Main (bootstrap) ----------
def gen_main():
    exts=[("Script","res://scripts/core/main_bootstrap.gd"),("PackedScene","res://scenes/menu/MainMenu.tscn")]
    n=""
    n+=node("Main","Node","","1")
    n+=node("MenuLayer","CanvasLayer","Main",props={"layer":"10"})
    n+=node("Menu","Node","MenuLayer")
    w("main/Main.tscn", HEAD.format(steps=2+len(exts)) +
      '[ext_resource type="Script" path="res://scripts/core/main_bootstrap.gd" id="1"]\n'
      '[ext_resource type="PackedScene" path="res://scenes/menu/MainMenu.tscn" id="2"]\n\n' + n)

gen_card(); gen_menu(); gen_hud(); gen_evo(); gen_pause(); gen_debug(); gen_main()
print("UI scene generation complete.")
