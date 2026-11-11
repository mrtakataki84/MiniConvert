# MiniConvert — Android video converter

Ini ialah **fasa 1: UI + pemilihan video + batch queue**.

## Apa yang sudah berfungsi
- UI Android minimalis.
- Pemilih video Android (boleh pilih banyak fail).
- Senarai queue dan buang item daripada queue.
- Pilihan format, codec, resolusi dan mod pemprosesan (UI sahaja).

## Belum disambungkan
Butang conversion belum menukar video lagi. Fasa seterusnya ialah mengintegrasikan enjin pemprosesan offline:
- FFmpeg untuk demuxing, muxing, filter, audio dan format.
- Android MediaCodec untuk hardware encoder/decoder apabila peranti menyokongnya.
- Queue worker, progress, cancel dan output melalui MediaStore.

Nota: hardware acceleration bukan jaminan untuk setiap codec atau telefon. Aplikasi perlu mengesan codec yang benar-benar disokong oleh peranti.

## Build APK tanpa Android Studio
1. Buat repository GitHub dan upload kandungan projek ini.
2. Push ke branch `main`, atau buka tab **Actions** dan jalankan `Build MiniConvert APK`.
3. Selepas workflow selesai, buka hasil run dan muat turun artifact `MiniConvert-debug`.
4. Extract ZIP artifact dan pasang `app-debug.apk` pada telefon untuk menguji UI.

## Versi build
- Android Gradle Plugin 9.3.0
- Kotlin 2.4.21
- Gradle 9.5.0
- JDK 17
- compileSdk 36, minSdk 26
