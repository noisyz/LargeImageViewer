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

Also allow creating custom methods.
Use
```java
largeImageView.loadImage(InputStream inputStream, int imageWidth, int imageHeight);
```
