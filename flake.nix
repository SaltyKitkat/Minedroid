{
  description = "my project description";

  inputs = {
    flake-utils.url = "github:numtide/flake-utils";
    this.url = "path:/home/syk/repos/flakes";
  };

  outputs = { self, nixpkgs, flake-utils, this }:
    flake-utils.lib.eachDefaultSystem
      (system:
        let pkgs = this.legacyPackages.${system}; in
        {
          devShells.default = import ./shell.nix { inherit pkgs; };
        }
      );
}
