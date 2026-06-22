{
	description = "Minecraft Fabric mod that makes anvil costs make more sense";

	inputs = {
		nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";
		flake-utils.url = "github:numtide/flake-utils";
	};

	outputs = {
		nixpkgs,
		flake-utils,
		...
	}: flake-utils.lib.eachDefaultSystem (system:
			let
				pkgs = nixpkgs.legacyPackages.${system};
			in {
				devShell = pkgs.mkShell {
					nativeBuildInputs = with pkgs; [
						git
						gradle_9
						openjdk25
					];

					env = {
						LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath (with pkgs; [
							libGL
							glfw3-minecraft
							vulkan-loader
							libpulseaudio
							flite
							udev
						]);
					};
				};
			});
}
