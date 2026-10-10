import os
import glob

old_pkg = "com.ivor.scale"
new_pkg = "duxtami.gramly.app"
directory = "/sdcard/termux/gramly"

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith((".kt", ".java", ".xml", ".kts", ".pro", ".properties")):
            filepath = os.path.join(root, file)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            if old_pkg in content:
                print(f"Updating {filepath}")
                content = content.replace(old_pkg, new_pkg)
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(content)
print("Done")
