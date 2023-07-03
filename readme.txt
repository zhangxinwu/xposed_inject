adb install xposed_tool_v3.0.0.apk
adb push classes.dex /sdcard/
adb push lib/arm64-v8a/libsandhook.so /sdcard/libsandhook64.so
adb push lib/armeabi-v7a/libsandhook.so /sdcard/libsandhook32.so
adb shell
---
cd /data/local/tmp
mkdir xposed
cd xposed
cp /sdcard/classes.dex .
cd /system/product/lib
cp /sdcard/libsandhook32.so libsandhook.so
chmod 644 libsandhook.so
cd ../lib64
cp /sdcard/libsandhook64.so libsandhook.so
chmod 644 libsandhook.so
echo "libsandhook.so" >> /system/etc/public.libraries.txt
reboot


去特征使用classes_unsign.dex
插件需要修改类名，使用vscode插件apklab反编译
手动替换字符串 
de.robv.android.xposed -> be.vbor.android.xxxsed
de/robv/android/xposed -> be/vbor/android/xxxsed

reference
https://github.com/WindySha/xposed_module_loader

