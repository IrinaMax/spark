# R on Spark

SparkR is an R package that provides a light-weight frontend to use Spark from R.

### SparkR development

#### Build Spark

Build Spark with [Maven](http://spark.apache.org/docs/latest/building-spark.html#building-with-buildmvn) and include the `-PsparkR` profile to build the R package. For example to use the default Hadoop versions you can run
```
  build/mvn -DskipTests -Psparkr package
```

#### Running sparkR

You can start using SparkR by launching the SparkR shell with

    ./bin/sparkR

The `sparkR` script automatically creates a SparkContext with Spark by default in
local mode. To specify the Spark master of a cluster for the automatically created
SparkContext, you can run

    ./bin/sparkR --master "local[2]"

To set other options like driver memory, executor memory etc. you can pass in the [spark-submit](http://spark.apache.org/docs/latest/submitting-applications.html) arguments to `./bin/sparkR`

#### Using SparkR from RStudio

If you wish to use SparkR from RStudio or other R frontends you will need to set some environment variables which point SparkR to your Spark installation. For example 
```
# Set this to where Spark is installed
Sys.setenv(SPARK_HOME="/Users/shivaram/spark")
# This line loads SparkR from the installed directory
.libPaths(c(file.path(Sys.getenv("SPARK_HOME"), "R", "lib"), .libPaths()))
library(SparkR)
sc <- sparkR.init(master="local")
```
#### Running SparkR Streaming

After starting the SparkR with `sparkR` script or RStudio or any other your favourite R frontends, you can initialize SparkR streaming with the Spark Context `sc` and the command
```
ssc <-sparkR.streaming.init(sc, batchDuration = 1L)
```

to start a Streaming Context `ssc`. Then you can create DStreams and apply transformations. You can start streaming by using command
```
startStreaming(ssc)
```

For window and state functions, you need to give SparkR streaming a checkpoint directory before starting streaming, by using command
```
checkpoint(ssc, "/Users/haolin/checkpoints")
```

#### Making changes to SparkR

The [instructions](https://cwiki.apache.org/confluence/display/SPARK/Contributing+to+Spark) for making contributions to Spark also apply to SparkR.
If you only make R file changes (i.e. no Scala changes) then you can just re-install the R package using `R/install-dev.sh` and test your changes.
Once you have made your changes, please include unit tests for them and run existing unit tests using the `run-tests.sh` script as described below. 
    
#### Generating documentation

The SparkR documentation (Rd files and HTML files) are not a part of the source repository. To generate them you can run the script `R/create-docs.sh`. This script uses `devtools` and `knitr` to generate the docs and these packages need to be installed on the machine before using the script.
    
### Examples, Unit tests

SparkR comes with several sample programs in the `examples/src/main/r` directory.
To run one of them, use `./bin/sparkR <filename> <args>`. For example:

    ./bin/sparkR examples/src/main/r/pi.R local[2]

To run SparkR Streaming wordcount example:

    ./bin/sparkR examples/src/main/r/streaming/hdfs_wordcount.R /home/haolin/rstreaming/

You can also run the unit-tests for SparkR by running (you need to install the [testthat](http://cran.r-project.org/web/packages/testthat/index.html) package first):

    R -e 'install.packages("testthat", repos="http://cran.us.r-project.org")'
    ./R/run-tests.sh

### Running on YARN
The `./bin/spark-submit` and `./bin/sparkR` can also be used to submit jobs to YARN clusters. You will need to set YARN conf dir before doing so. For example on CDH you can run
```
export YARN_CONF_DIR=/etc/hadoop/conf
./bin/spark-submit --master yarn examples/src/main/r/pi.R 4
```