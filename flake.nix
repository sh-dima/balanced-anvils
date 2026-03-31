{
	description = "Makes anvil experience costs make more sense.";

	inputs = {
		nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11";
		flake-utils.url = "github:numtide/flake-utils";
	};

	outputs = {
		nixpkgs,
		flake-utils,
		...
	}: flake-utils.lib.eachDefaultSystem (system:
			let
				pkgs = import nixpkgs {
					inherit system;
				};
			in {
				devShell = pkgs.mkShell {
					nativeBuildInputs = with pkgs; [
						git
						gradle_9
					];

					env = {
						LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath (with pkgs; [
							libGL
							glfw3-minecraft
							libpulseaudio
							flite
							udev
						]);
					};
				};
			});
}
