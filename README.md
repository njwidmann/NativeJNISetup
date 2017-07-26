# How to setup Native C++ in a Java project with JNI on Windows
This setup overview is based on [a Tutorial by IBM Developer Works](https://www.ibm.com/developerworks/java/tutorials/j-jni/j-jni.html).

**Prerequisites:**

To setup JNI, you will need the following tools and components:

* **A Java compiler:** javac.exe ships with the SDK.
* **A Java virtual machine (JVM):** java.exe ships with the SDK.
* **A native method C file generator:** javah.exe ships with the SDK.
* **Library files and native header files** that define JNI. The jni.h C header file, jvm.lib, and jvm.dll or jvm.so files all ship with the SDK.
* **A C and C++ compiler** that can create a shared library. The two most common C compilers are Visual C++ for Windows and cc for UNIX-based systems.


**Step 1) Create Java file (eg Sample1.java) with native methods in following format:**
```
public class Sample1
{
	public native int intMethod(int n);
	public native boolean booleanMethod(boolean bool);
	public native String stringMethod(String text);
	public native int intArrayMethod(int[] intArray);

	public static void main(String[] args)
	{
		System.loadLibrary("Sample1");
	}
}
```

**Step 2) Generate header file (eg Sample1.h):**
in terminal:

* First navigate to working directory. For example:
`cd C:\Users\Nick\IdeaProjects\native_test\src`

* Then generate header:
`javah Sample1`

Note: this works because I added "C:\Program Files\Java\jdk1.8.0_20\bin" to Path Environment Variable


**Step 3) Create c++ file implementing native methods. For example (Sample1.cpp):**
```
#include "Sample1.h"
#include <string.h>

JNIEXPORT jint JNICALL Java_Sample1_intMethod
(JNIEnv *env, jobject obj, jint num) {
	return num * num;
}

JNIEXPORT jboolean JNICALL Java_Sample1_booleanMethod
(JNIEnv *env, jobject obj, jboolean boolean) {
	return !boolean;
}

JNIEXPORT jstring JNICALL Java_Sample1_stringMethod
(JNIEnv *env, jobject obj, jstring string) {
	const char *str = env->GetStringUTFChars(string, 0);
	char cap[128];
	strcpy(cap, str);
	env->ReleaseStringUTFChars(string, str);
	return env->NewStringUTF(strupr(cap));
}

JNIEXPORT jint JNICALL Java_Sample1_intArrayMethod
(JNIEnv *env, jobject obj, jintArray array) {
	int i, sum = 0;
	jsize len = env->GetArrayLength(array);
	jint *body = env->GetIntArrayElements(array, 0);
	for (i=0; i<len; i++) {   
		sum += body[i];
	}
	env->ReleaseIntArrayElements(array, body, 0);
	return sum;
}

void main(){}
```

**Step 4) Generate your DLL (shared library files):**

* Open "Developer Command Prompt for VS 2017" for x86 or "x64 Native Tools Command Prompt for VS 2017" for x64.
I use x64

* Navigate to working directory. ex: 
`cd C:\Users\Nick\IdeaProjects\native_test\src`

* Generate shared library files (you need to do this everytime you update your .cpp files):
`cl -I"C:\Program Files\Java\jdk1.8.0_20\include" -I"C:\Program Files\Java\jdk1.8.0_20\include\win32" -LD Sample1.cpp -FeSample1.dll`

Note: your includes (`-I"..."`) might be different depending on the location and version of your jdk


**Step 5) Include the following code in java file (eg Sample1.java) to link to library:**

* Before `"System.loadLibrary(...)"` :
```	
String cwd = System.getProperty("user.dir") + "/src";

try {
	addLibraryPath(cwd);
} catch(Exception ex) {
	ex.printStackTrace();
}
```

* Define addLibraryPath() so that you can modify java.library.path at runtime:
```
/**
 * Adds the specified path to the java library path
 *
 * @param pathToAdd the path to add
 * @throws Exception
 */
public static void addLibraryPath(String pathToAdd) throws Exception {
  final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
  usrPathsField.setAccessible(true);

  //get array of paths
  final String[] paths = (String[]) usrPathsField.get(null);

  //check if the path to add is already present
  for (String path : paths) {
    if (path.equals(pathToAdd)) {
      return;
    }
  }

  //add the new path
  final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
  newPaths[newPaths.length - 1] = pathToAdd;
  usrPathsField.set(null, newPaths);
}
```
	
	
**Step 6) Run project!**


**External Libraries**

You can include external libraries, such as OpenCV, by linking them when you compile your DLL. For example:
```
cl /I"C:\Program Files (x86)\Java\jdk1.8.0_131\include\win32" /I"C:\Program Files (x86)\Java\jdk1.8.0_131\include" /I"C:\opencv\build\include" /D_USRDLL /D_WINDLL Sample1.cpp C:\opencv\build\x86\vc11\lib\opencv_core2413.lib C:\opencv\build\x86\vc11\lib\opencv_highgui2413.lib C:\opencv\build\x86\vc11\lib\opencv_imgproc2413.lib /link /DLL /OUT:Sample1.dll
```
Then make sure load to DLLs for any libraries you use. For example:
```
//Load DLLS
System.loadLibrary("OpenCV/opencv_core2413");
System.loadLibrary("OpenCV/opencv_highgui2413");
System.loadLibrary("OpenCV/opencv_imgproc2413");
System.loadLibrary("Sample1");
```
