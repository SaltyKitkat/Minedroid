{ pkgs? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs =  with pkgs; [
    android-studio
  ];
}
