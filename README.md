Akka MapReduce Sample
==============================

This sample provides a generic approach to create a three layer map reduce system using 3 kinds of actors:
- Map
- Reduce
- Aggregate

It uses an example with an SQL Map but I personally use this method on CQL (Cassandra) where the langage doesn't provide as much flexibility as SQL.

This method is really useful on huge datasets, I need it to reduce millions of rows from many thousands of CQL requests on a cluster for REAL TIME PROCESSING.
This will often push to its limits your machine if your map is fast enough. The production application often reachs 100% on 8 cores to achieve a fast result on a single execution.

They are no "magic value" to choose your round robin router sizes and map key sizes... it usually depend on your cluster and the machine you use for the scala App... try to benchmark :)

Using it
--------

* Download [the latest playframework version] (http://www.playframework.com/). The version 2.2.1 is used in this sample.
* Run this application with it ('play run' in the root directory) 
* Go to [http://localhost:9000/](http://localhost:9000/)

Libraries used
--------
* [Play Framework] (http://www.playframework.com/)
* [Akka] (http://akka.io/)
* [Bootstrap] (http://getbootstrap.com/)

Contact Me
--------

* [@Kayrnt](https://twitter.com/Kayrnt)
* [Porfolio](http://www.kayrnt.fr)

Licence
-------

This software is licensed under the Apache 2 license, quoted below.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.