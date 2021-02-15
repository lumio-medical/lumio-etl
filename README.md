# Lumio ETL

[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/lumio-medical/lumio-etl/Java%20CI%20with%20Maven)](https://github.com/lumio-medical/lumio-record/actions?query=workflow%3A%22Java+CI+with+Maven%22)
[![Maven Central Repository](https://maven-badges.herokuapp.com/maven-central/com.lumiomedical/lumio-etl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.lumiomedical/lumio-etl)
[![javadoc](https://javadoc.io/badge2/com.lumiomedical/lumio-etl/javadoc.svg)](https://javadoc.io/doc/com.lumiomedical/lumio-etl)
![GitHub](https://img.shields.io/github/license/lumio-medical/lumio-etl)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Flumio-medical%2Flumio-etl.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Flumio-medical%2Flumio-etl?ref=badge_shield)

This library provides utilities for building `lumio-flow` based ETLs.

It is important to note that this library was very much a work in progress in both spirit and body.
The general concept is here: it is an aggregate of general-purpose implementations of `lumio-flow` actors with the intended goal of enabling the implementation of simple yet flexible ETL programs.
However, much was still being worked on, or left to be worked on later, and so there are many shortcomings to keep in mind when reading what is here.

1. The "best" level of abstraction to design actors is not decided: sometimes it is good to have small atomic operations (eg. reading a file on a filesystem or from AWS), sometimes it is good to
have high-level operations encompassing a diversity of actions. So far, ETL programs implemented with this project have been a mix of both what I would consider successes as well as design failures.
My belief was that this API needed a higher-level abstraction API for `lumio-flow` DAG building in combination with well-designed actors ; sometimes the issue is not the scope of an actor itself as
much as how fragmented its manipulation can be. Maybe some kind of "actor recipes" could also be a part of the solution.
2. The implementations of actors can be minimalist at times, reflecting the level of sophistication that was needed in the project for which it was developed, this is generally not really an issue,
new features can be added.
3. As it stands, there are also a handful of basic implementations for things that aren't exactly ETL-related (eg. most things in `transformer.text`, or `transformer.jsoup` for instance), these
could end up being scrapped at a later date.

Implementations found in this package shouldn't be tied to any specific Lumio project.

_Note: This library is considered as "in beta" and as such significant API changes may occur without prior warning._

## I. Installation

Add the following in your `pom.xml`:

```xml
<dependency>
    <groupId>com.lumiomedical</groupId>
    <artifactId>lumio-etl</artifactId>
    <version>0.5</version>
</dependency>
```

## II. Notes on Structure and Design

There are four facilities in this library:

* The `ETL` class is a higher level abstraction for `lumio-flow` DAGs meant to be a space for declaring DAG structure, host configuration parameters, as well as specify which `FlowCompiler` to use.
All ETL pipelines used throughout `lumio-core` use this class as a base.
* Anything in the `dataframe` package pertains to the manipulation of `tech.tablesaw` dataframes. It provides notably helper functions, and the `TableProcessor` feature, which was meant to be used
in conjunction with a higher abstraction for `lumio-flow` (essentially, a mean to specify a specific dataframe refinement stage). It is possible that the final implementation for that idea would have
ended up rejecting the `TableProcessor` contract in favour of `lumio-flow` actor contracts (it would have certainly been preferable at least).
* The `extractor`, `generator`, `loader` and `transformer` packages provide `lumio-flow` actor implementations
* The `vault` package provides a handful of `VaultModule` implementations for handling custom configuration. It should be noted that as it stands, these were also very much work in progress: the general
idea was to start with satisfying the need for conf-based ETL pipeline definition (at least their entry-points, in order to allow swapping between loading files from an AWS instance or a filesystem for instance)
and tackle "prettiness" later. As it stands it is fairly verbose for pipelines with many data sources, I believe just building a more opinionated abstraction on top of it could go a long way.

_TODO_

## III. Usage

Note that two sample "toy" programs are also provided: `sample-nlp` [here](./sample/nlp) and `sample-crawl` [there](./sample/crawl).
None of them leverage `lumio-vault` [configuration features](https://github.com/lumio-medical/lumio-vault), but their structure could be simplified and made more resilient to changes with a bit of `lumio-vault` sprinkled in.

We'll also write down a basic example of ETL pipeline leveraging some features found in this library, we won't touch on the `ETL` classes, these are covered in the sample project.

Most of the syntax is actually from `lumio-flow`, it could be a good idea to start by having a look at it [there](https://github.com/lumio-medical/lumio-flow).

Let us start by imagining we have a tiny CSV dataset like this:

```csv
key,value,metadata
0,234,interesting
1,139,not_interesting
3,982,interesting
```

Here is what a pipeline for manipulating this could look like:

```java
var flow = Flow
    .from(new FileStreamer(), "path/to/my.csv") //We open an inpustream from the CSV file
    .pipe(new TablesawCSVParser()) //We interpret it as CSV and transform it into a tablesaw dataframe
    .pipe(Tablesaw::print) // We print the dataframe to stdout
;

Flow.runAsPipeline(flow);
```

Running the above should display the following, granted a logger configured for printing `INFO` level information:

```log
[main] INFO com.lumiomedical.etl - Initializing stream from filesystem at data/my.csv
[main] INFO com.lumiomedical.etl - Extracting CSV data into dataframe...
[main] INFO com.lumiomedical.etl - Extracted 3 lines into dataframe.
                                               
 index  |  key  |  value  |     metadata      |
-----------------------------------------------
     0  |    0  |    234  |      interesting  |
     1  |    1  |    139  |  not_interesting  |
     2  |    3  |    982  |      interesting  |
(row_count=3)
```

Note that it added an `index` column, we can remove it by specifying a `TableProperties` object with `setAddRowIndex(false)`.
Let's also add a filter, and a persistence operation:

```java
var tableProperties = new TableProperties().setAddRowIndex(false);

var flow = Flow
    .from(new FileStreamer(), "path/to/my.csv")
    .pipe(new TablesawCSVParser(tableProperties))
    .pipe(Criterion.whereIsEqualTo("metadata", "interesting")) //We use a helper query feature, note that there are many other ways to do that, notably using the tablesaw API
    .sink(new TablesawCSVWriter("path/to/my-filtered.csv")) //We dump the dataframe as CSV into another file
;

Flow.runAsPipeline(flow);
```

Upon running, the above should produce a CSV file like this one:

```csv
key,value,metadata
0,234,interesting
3,982,interesting
```

Will wrap-up this very simple example by replacing the source by one loading the file from AWS:

```java
var tableProperties = new TableProperties().setAddRowIndex(false);

var flow = Flow
    .from(new AmazonS3Streamer(s3, "my-bucket", "my.csv")) // Given a properly configured AmazonS3 instance
    .pipe(new TablesawCSVParser(tableProperties))
    .pipe(Criterion.whereIsEqualTo("metadata", "interesting"))
    .sink(new TablesawCSVWriter("path/to/my-filtered.csv")) // We still write the output to the filesystem
;

Flow.runAsPipeline(flow);
``` 

As the reader can guess, the general idea is to define the execution plan (general structure and type transitions) separately from the choice of implementation used for performing the transformations.
For instance, here, we would likely make the `Extractor` and `Loader` swappable, while retaining the interpretation as a CSV and subsequent filtering.
Some situations may call for entire little pipelines with remote extracting, unzipping, streaming, etc.
The goal was to make it possible to focus on the core logic and retain control over how the pipeline interacts with the outside world.

_TODO_

## IV. Dev Installation

This project will require you to have the following:

* Java 11+
* Git (versioning)
* Maven (dependency resolving, publishing and packaging) 


## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Flumio-medical%2Flumio-etl.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Flumio-medical%2Flumio-etl?ref=badge_large)