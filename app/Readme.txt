Le nom d'un fichier image est composé du mode (son id) et du theme (son id).
Les id sont définis dans le fichier configuration.
Si un id est modifié dans le fichier de configuration, les noms des 3 fichiers image doivent être modifiés en conséquence.
---
Pour la gestion des versions, la doc Android signale que ce qu'ils utilisent c'est le "versionCode".
Le "versionName" est ce qui apparait quand on va dans "about". Il n'est pas utilisé par Android pour gérer les versions. C'est juste une info à l'intention des utilisateurs.
"versionCode" et "versionName" sont définis dans le fichier "build.gradle" (module)
Attention le "versionName" est aussi défini dans le fichier de configuration.

Build pour AIR³ 7.2: minSdkVersion 27
Build pour AIR³ 7.3: minSdkVersion 29
Build pour AIR³ 7.35:minSdkVersion 31

Les différences entre les versions 7.2, 7.3 et 7.35:
- reset contient les éléments de Keybindings : different keys

- reset contient la valeur de smoothing:
7.2: "Sensors.Barometer.LowPassFilterWeight": 0.03
7.3: "Sensors.Barometer.LowPassFilterWeight": 0.075
7.35: Attention depuis XCTrack 0.9.11.4 beta, l'information dans le fichier config est différent! 0.07

- reset: path pour Roadmap
7.2: "/storage/emulated/0/XCTrack/Map/RoadMap"
7.3 et 7.35: "/storage/emulated/0/Android/data/org.xcontest.XCTrack/files/Map/RoadMap"

- Pour les fichiers themes et Reset:
7.2: "Mapsforge.ThemeFile": "/storage/emulated/0/XCTrack/Map/themes/hyperpilot-bigger-cities.xml",
7.3 et 7.35: "Mapsforge.ThemeFile": "/storage/emulated/0/Android/data/org.xcontest.XCTrack/files/Map/themes/hyperpilot-bigger-cities.xml",