# Etil
Simple Mapper for Cursor to Model and Model to ContentValues. <br>
Etil uses Annoation Processing to generate the mapping instead of using reflection, 
which means you will have the same performance as manually turning a Cursor to a Model or a Model to ContentValues.

## Usage

##### Annotate the model

For example:

```java
@EtilTable("pet")
public class Pet  {

    @EtilField("_id")
    public long id;

    @EtilField("name")
    public String name;
    
    @EtilField("age")
    public String age;
}
```

Supported Datatypes: 
* Primitve Types: int, long,  float, double, boolean
* Types: String, Intger, Long, Float, Double, Boolean

It is assumed that you use integer values (0 or 1) to represent boolean values in your sqlite databse. <br>
"_id" fields are not added to the ContentValues when converting a model to their ContentValues.

##### Cursor -> Model

For example:

```java
Cursor petCursor = ...;
Pet pet = EtilMapper.mapCursorToModel(Pet.class, petCursor);
```

"mapCursorToModel" simply extracts the data from the current Cursor position and doesn't touch the state of the cursor, 
closing the cursor or calling moveToNext() is your responsibility. <br>
Calling with a Class<T> that doesnt have the @EtilTable("...") annoation will result in a IllegalArgumentException.


##### Model -> ContentValues
For example:

```java
Pet pet = new Pet();
pet.name = "Dogmeat";
pet.age = 12;

ContentValues petContentValues =  EtilMapper.mapModelToContentValues(pet);
```
Calling with a parameter that doesnt have the @EtilTable("...") annoation will result in a IllegalArgumentException.

## Download

##### Gradle
In your build.gradle in your root folder:

```gradle
repositories {
	maven { url "https://jitpack.io" }
}
```
In your build.gradle in your app folder:

```gradle
apply plugin: 'com.neenbedankt.android-apt'
​
dependencies {
	  compile 'com.github.tractive.etil:etil-annotations:v0.4'
	  apt 'com.github.tractive.etil:etil-compiler:v0.4'
}
```
For the current Version check the releases.


