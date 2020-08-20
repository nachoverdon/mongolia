# Mongolia

A Magnolia CMS utilities library

### Install

1. Create the `.jar`:
    ```
    mvn clean install
    ```

1. Install it on Maven:

    ```
    mvn install:install-file -Dfile=mongolia-1.0-SNAPSHOT.jar -DgroupId=com.nachoverdon -DartifactId=mongolia 
    -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
    ```

1. Add it as a dependency in your module:

    ```xml
    <dependencies>
        <dependency>
          <groupId>com.nachoverdon</groupId>
          <artifactId>mongolia</artifactId>
          <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ```

1. To use `MongoliaTemplatingFunctions`, add it to your `yourproject-module.xml`:
    
    1. Add it 
        ```xml
        <components>
            <component>
              <type>com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions</type>
              <implementation>com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions</implementation>
              <scope>singleton</scope>
            </component>
        </components>
        ```
    1. Add it to your config:
        ```
            modules:
                rendering:
                    renderers:
                        freemarker:
                            contextAttributes:
                                mongofn:
                                    componentClass: com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions
                                    name: mongofn
        ```
    1. Add it to `freemarker_implicit.ftl`:
        ```
        [#-- @ftlvariable name="mongofn" type="com.nachoverdon.mongolia.templating.MongoliaTemplatingFunctions" --]
        ```

### Generating JavaDoc

To generate the JavaDoc you need to use the following command:

```
mvn javadoc:javadoc
```

It will generate `target/site/apidocs/`. To navigate the docs, just open `index.html`.

### Style checking

To check the style of the Java code you need to use the following command:

```
mvn checkstyle:check
```

Please, check the style of your code and fix any issue before committing your changes.

### To do:

- [ ] Tests
- [ ] Allow accents on JCR queries.
- [ ] Pagination utilities
- [ ] Menu app
- [ ] Freemarker utilities
- [ ] YAMLs to include on dialogs
- [ ] Servlets utilities
- [ ] i18n app (messages.properties, export to/from csv, etc...)
- [ ] VirtualUri utilities
- [ ] Groovy scripts
- [ ] Email utilities
- [ ] Publish to Maven
- [ ] Node2Bean using YAML/JSON dynamically.
