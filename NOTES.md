Declare a map
```
def obj = [a: '1']
```
https://stackoverflow.com/questions/9312805/is-it-possible-to-create-object-without-declaring-class


To define a variable inside the vars, use groovy.transform.Field:

```
import groovy.transform.Field
@Field List awe = [1, 2, 3]
def awesum() { awe.sum() }
assert awesum() == 6
```
https://stackoverflow.com/questions/40227332/jenkins-shared-pipeline-library-can-i-declare-static-variables-in-vars-file
http://docs.groovy-lang.org/latest/html/gapi/groovy/transform/Field.html
