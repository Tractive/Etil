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

##Advanced Usage
You can also inherited model classes some models use the same fields. For example:

```java
public class Animal  {
    @EtilField("_id")
    public long id;

    @EtilField("name")
    public String name;
}

@EtilTable("dog")
public class Dog extends Animal {
    @EtilField("has_a_tracker")
    public boolean hasATracker;
}

@EtilTable("cat")
public class Cat extends Animal {
    @EtilField("secretly_plots_to_kill_you")
    public boolean secretlyPlotsToKillYou;
}
```
You have 2 tables in your database - Cat and Dog - which shares some fields in the Animal class. If you try to convert a Cursor to Cat object Etil will set "id", "name" and "secretlyPlotsToKillYou".

Even works with more than one inheritance:

```java
public class Animal  {
    @EtilField("_id")
    public long id;

    @EtilField("name")
    public String name;
}

public class Mammal extends Animal {
    @EtilField("has_a_tracker")
    public boolean hasATracker;
}

@EtilTable("cat")
public class Cat extends Mammal {
    @EtilField("secretly_plots_to_kill_you")
    public boolean secretlyPlotsToKillYou;
}
```
Converting Cursor to Cat will set "id", "name", "hasATracker" and "secretlyPlotsToKillYou".

##### Model from multiple Tables
Sometime you have more complicated SQL queries that creates cursor object that has columns from multiple teams. In this case you can indicate via @MuliEtilTable that the model has more than one table. For example

```java
@MultiEtilTable
public class MammalCat  {
 
    @EtilField("secretly_plots_to_kill_you")
    public boolean secretlyPlotsToKillYou;
    
    @EtilField("has_a_tracker")
    public boolean hasATracker;
}
```
In this case the getTableNameFromModel and getTableNameFromModelClass will just throw an error as you don't have a single table for this model. Currently there is no support for multi table model that have the same column name. You can however change your column name via "AS" in your sql statement. 

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
	  compile 'com.github.tractive.etil:etil-annotations:v0.5.1'
	  apt 'com.github.tractive.etil:etil-compiler:v0.5.1'
}
```
For the current Version check the releases.

##TODO

#####Testing
#####Javadoc + General Comments
#####More Comlicated Inheritance
For example:
```java
@EtilTable("animal")
public class Animal  {
    @EtilField("_id")
    public long id;

    @EtilField("name")
    public String name;
}

@EtilTable("cat")
public class Cat extends Animal {
    @EtilField("secretly_plots_to_kill_you")
    public boolean secretlyPlotsToKillYou;
}
```
If cat extends from animal and both are tables in your database (as indicated by the EtilTable annoation), it won't work properly. (if you have that specific case for whatever reason)
