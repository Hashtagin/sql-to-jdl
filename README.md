
# sql-to-jdl From mysql and postgres
Tool to translate SQL databases to JDL format of jHipster (Created due to existing databases to be generated with jHipster and build angular-java web)

# Why not use tools like UML provided on jHipster?
- JDL from web is ok for a few entities but not for more than 100 entities and relations
- UML software and xml exporters could have worked (other tools on jHipster) but:
  - already many databases in production to be exported in JDL (faster to generate the JDL from it)
  - already working UML design with MySQL Workbench

# How to use

1. Run "mvn compile" at least once to let jOOQ generate some required Classes (see [Issue solved](https://github.com/Blackdread/sql-to-jdl/issues/2)).

2. Set properties file:
    - Schema name to export
    - Tables names to be ignored
    - Path of export file

3. Run spring-boot run goal to generate jdl according to properties setuped

# After JDL file is generated
Still have some manual steps to do:
- review relations:
  - ManyToMany
  - Owner side display field
  - Inverse side field name and display field
  - Bidirectional or not
- add values to enums
- review validations of entities

# Use of
- jOOQ
- Spring boot

# Default specific rules
Table is treated as enum if only 2 columns and both are: "id" AND ("code" OR "name")

Table is treated as ManyToMany if only 2 columns and both are foreign keys

# TODO LIST
   - [ ] Have all postgresql types supported.

# Links
[jHipster JDL](http://www.jhipster.tech/jdl/)

# Forked from [SQL-To-JDL by BlackDread](https://github.com/Blackdread/sql-to-jdl)