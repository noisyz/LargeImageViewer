# LargeImageViewer
Load large images with LargeImageView

Gradle:

```gradle
compile 'com.noisyz.largeimageviewer:largeimageviewlibrary:1.0.0'
```

Maven:

```maven
<dependency>
  <groupId>com.noisyz.largeimageviewer</groupId>
  <artifactId>largeimageviewlibrary</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

Use following methods to load large images.

```java
LargeImageView largeImageView = (LargeImageView) findViewById(R.id.largeImageView);
largeImageView.setImageResource(R.drawable.very_large_image);
```
or
```java
...
largeImageView.setImageFromAssets("path/to/your/asset");
...
///propably you can catch a IllegalArgumentException
largeImageView.setImageFromFile(yourFile);
...
```

Also allows creating custom methods.
Use
```java
largeImageView.loadImage(InputStream inputStream, int imageWidth, int imageHeight);
```
Licence

Copyright 2016 Oleg Bohdan

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
