#s/\/home\/jka\/src\/android\/Librefree/$APPDIR/g
APPDIR=/home/jka/src/android/Glucodata
#ABI=armeabi-v7a

ABI=$1
OUTPUTDIR=$APPDIR/app/build/mij/debug
cmake\
	-H$APPDIR/app/src/main/cpp\
	-DCMAKE_CXX_FLAGS=-std=c++20\
	-DCMAKE_FIND_ROOT_PATH=$OUTPUTDIR\
	-DCMAKE_BUILD_TYPE=Debug\
	-DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake\
	-DANDROID_ABI=$ABI\
	-DANDROID_NDK=$ANDROID_NDK\
	-DANDROID_PLATFORM=android-21\
	-DCMAKE_ANDROID_ARCH_ABI=$ABI\
	-DCMAKE_ANDROID_NDK=$ANDROID_NDK\
	-DCMAKE_EXPORT_COMPILE_COMMANDS=ON\
	-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=$OUTPUTDIR\
	-DCMAKE_SYSTEM_NAME=Android\
	-DCMAKE_SYSTEM_VERSION=19\
	-DCMAKE_VERBOSE_MAKEFILE=1\
	-B$OUTPUTDIR\
	-G'Unix Makefiles'


