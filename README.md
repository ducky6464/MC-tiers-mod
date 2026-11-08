# MC-Tiers Fabric Mod

Client-side Fabric mod for Minecraft 1.21.11. It looks up MC-Tiers rankings and adds a cached tier label to player nameplates. Java 21, Fabric Loom 1.14, Fabric API 0.141.3+1.21.11.

## Game mode selector

Press **K** in-game to open the compact MC-Tiers mode selector. Your selected mode is saved in `config/mctiers.properties`.

The mod downloads the public game-mode catalog from `https://mc-tiers.pages.dev/api/public/v1/modes` when it starts. New modes added to the website appear in the selector automatically. A built-in list of current modes is used if the website is temporarily unavailable.

Mode markers use colored text glyphs instead of vanilla item textures, so resource packs that change the End Crystal or other item textures do not replace the MC-Tiers marker.
