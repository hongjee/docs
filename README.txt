We have a biller data set in JSON formats.
You can use the data set to index documents to Solr.

This data consists of the following fields:
 * "id" - unique identifier for the biller
 * "name" - name of the biller
 * "zip" - zip code of the biller
 * "servicing_zip" - servicing area in zip list
 * "popularity_zip" - popluarity by zip
        zip
        popularity

 Steps:
   * Start Solr:
       bin/solr start

   * Create a "billers" core/collection:
       bin/solr create -c billers

   * Set the schema of the core/collection:
        http://www.solrtutorial.com/basic-solr-concepts.html
        https://lucene.apache.org/solr/guide/8_0/indexing-nested-documents.html

  <field name="id" type="string" multiValued="false" indexed="true" required="true" stored="true"/>
  <field name="name" type="text_general" multiValued="false" required="true" stored="true"/>
  <field name="zip" type="string" multiValued="false" indexed="true" required="true" stored="true"/>



curl http://localhost:8983/solr/billers/schema -X POST -H 'Content-type:application/json' --data-binary '{
    "add-field" : {
        "name":"name",
        "type":"text_general",
        "multiValued":false,
        "stored":true
    },
    "add-field" : {
        "name":"zip",
        "type":"string",
        "indexed":true,
        "multiValued":false,
        "stored":true
    },
    "add-field" : {
        "name":"servicing_popularity_zip.zip",
        "type":"string",
        "indexed":true,
        "multiValued":true,
        "stored":true
    },
    "add-field" : {
        "name":"servicing_popularity_zip.popularity",
        "type":"plongs",
        "indexed":true,
        "multiValued":true,
        "stored":true
    }
}'

   * Now let's index the data

     bin/post -c billers example/biller/billers.json

        family@family-PC-linux:~/solr-8.5.1$ bin/post -c billers example/biller/billers.json
        java -classpath /home/family/solr-8.5.1/dist/solr-core-8.5.1.jar -Dauto=yes -Dc=billers -Ddata=files org.apache.solr.util.SimplePostTool example/biller/billers.json
        Picked up _JAVA_OPTIONS:   -Dawt.useSystemAAFontSettings=gasp
        SimplePostTool version 5.0.0
        Posting files to [base] url http://localhost:8983/solr/billers/update...
        Entering auto mode. File endings considered are xml,json,jsonl,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log
        POSTing file billers.json (application/json) to [base]/json/docs
        1 files indexed.
        COMMITting Solr index changes to http://localhost:8983/solr/billers/update...
        Time spent: 0:00:00.900

   * Let's get searching!
     - Search for 'Verizon':
       curl "http://localhost:8983/solr/billers/query?q=name:Verizon"

     - Show me all servicing zip '10001':
       curl "http://localhost:8983/solr/billers/query?q=servicing_popularity_zip.zip:10001&fl=*,score"

     - simple boosts by popularity
       curl "http://localhost:8983/solr/billers/query?q=servicing_popularity_zip.zip:10001&fl=*,score"

          {!boost b=servicing_popularity_zip.popularity}servicing_popularity_zip.zip:10001

https://www.drupal.org/project/search_api_solr/issues/2804067
https://lucene.apache.org/solr/guide/6_6/spatial-search.html
https://lucene.apache.org/solr/guide/7_2/other-parsers.html#function-query-parser
https://lucene.apache.org/core/3_6_0/api/core/org/apache/lucene/search/package-summary.html#changingSimilarity
https://lucene.apache.org/solr/guide/7_2/function-queries.html#function-queries

https://cwiki.apache.org/confluence/display/solr/SolrRelevancyFAQ#How_can_I_change_the_score_of_a_document_based_on_the_.2Avalue.2A_of_a_field_.28say.2C_.22popularity.22.29
        nested documents can be used so for the document
        we can created child document with the key & value as fields for each key,
        using block join queries will close to loop and give the ability to search document with a nested document matching the query.
http://yonik.com/solr-nested-objects/
                 Nested Documents and BlockJoinQuery

                bin/solr stop -all
                bin/solr start

                bin/solr create -c billers


curl http://localhost:8983/solr/billers/update?commitWithin=3000 -d '{delete:{query:"*:*"}}'

curl http://localhost:8983/solr/billers/update?commitWithin=3000 -d '
[
  {
    "id": "1000001",
    "name": "Verizon wireless",
    "zip": "10001",
    "_childDocuments":[
        {"id":"1000001_10001","zip":"10001", "popularity":100},
        {"id":"1000001_10002","zip":"10002", "popularity":1},
        {"id":"1000001_10003","zip":"10003", "popularity":10},
        {"id":"1000001_10004","zip":"10004", "popularity":2},
        {"id":"1000001_10005","zip":"10005", "popularity":3}
    ]
  },
  {
    "id": "1000002",
    "name": "Verizon wireless",
    "zip": "20002",
    "_childDocuments":[
        {"id":"1000002_20001","zip":"20001", "popularity":10},
        {"id":"1000002_20002","zip":"20002", "popularity":100},
        {"id":"1000002_20003","zip":"20003", "popularity":10},
        {"id":"1000002_20005","zip":"20005", "popularity":3}
    ]
  }
]'


                curl http://localhost:8983/solr/billers/update?commitWithin=3000 -d '{delete:{query:"*:*"}}'

                #### bin/post -c billers example/biller/billers.json

                curl http://localhost:8983/solr/billers/update?commitWithin=3000 -d @example/biller/billers.json


                curl "http://localhost:8983/solr/billers/query?q=*:*&fl=id"
                curl "http://localhost:8983/solr/billers/query?q=*:*&fl=*,score"


Block Join Query
        https://lucene.apache.org/solr/guide/7_6/other-parsers.html#block-join-parent-query-parser
        This parser takes a query that matches child documents and returns their parents.
        The syntax for this parser is similar: q={!parent which=<allParents>}<someChildren>.

#### search and boosted by servcing zip and popularity
curl http://localhost:8983/solr/billers/query -d '
q=name:verizon OR {!parent which=name:verizon score=total}{!boost b=popularity}zip1:10001&
fl=*,score'



        How can I change the score of a document based on the *value* of a field (say, "popularity")
        Use a FunctionQuery as part of your query.

        Solr can parse function queries in the following syntax.

        Some examples...

          # simple boosts by popularity
          defType=lucene&df=text&q=%2Bsupervillians+_val_:"popularity"
          defType=dismax&qf=text&q=supervillians&bf=popularity
          q={!boost b=popularity}text:supervillians

          # boosts based on complex functions of the popularity field
          defType=lucene&q=%2Bsupervillians+_val_:"sqrt(popularity)"
          defType=dismax&qf=text&q=supervillians&bf=sqrt(popularity)
          q={!boost b=sqrt(popularity)}text:supervillians
        These functions can also operate on an ExternalFileField



        if you type field is pre-determined text field ex type [compact, sedan, hatchback], I think you have to boost with query type field (q) to
        get more accurate boosting.

        Ex: http://localhost:8983/solr/my/select?q=type:sedan^100 type:compact^10 (:*)^1&wt=json&indent=true&fl=,score&debug=results&bf=recip(rord(publish_date),1,2,3)^1.5&sort=score desc

        For publish_date, replace with the date you use for getting latest resultes.

        In the above query, things to note is that
         - fl=,score -> The result set would display score value for each document
         - sort by score as first sort field that will give you the documents with the highest boost value (score) on top

         Play around with the boosting values ^100 ^10 (perhaps 5,10,20 ) and observe how the score value will change the documents.

         I'm not really sure how solr calculation works, however the above query must give you the accurate boosted documents.








Exploring the data further -

  * Increase the MAX_ITERATIONS value, put in your freebase API_KEY and run the film_data_generator.py script using Python 3.
    Now re-index Solr with the new data.

FAQ:
  Why override the schema of the _name_ and _initial_release_date_ fields?

     Without overriding those field types, the _name_ field would have been guessed as a multi-valued string field type
     and _initial_release_date_ would have been guessed as a multi-valued pdate type.  It makes more sense with this
     particular data set domain to have the movie name be a single valued general full-text searchable field,
     and for the release date also to be single valued.

  How do I clear and reset my environment?

      See the script below.

  Is there an easy to copy/paste script to do all of the above?

    Here ya go << END_OF_SCRIPT

bin/solr stop
rm server/logs/*.log
rm -Rf server/solr/films/
bin/solr start
bin/solr create -c films
curl http://localhost:8983/solr/films/schema -X POST -H 'Content-type:application/json' --data-binary '{
    "add-field" : {
        "name":"name",
        "type":"text_general",
        "multiValued":false,
        "stored":true
    },
    "add-field" : {
        "name":"initial_release_date",
        "type":"pdate",
        "stored":true
    }
}'
bin/post -c films example/films/films.json
curl http://localhost:8983/solr/films/config/params -H 'Content-type:application/json'  -d '{
"update" : {
  "facets": {
    "facet.field":"genre"
    }
  }
}'

# END_OF_SCRIPT

Additional fun -

Add highlighting:
curl http://localhost:8983/solr/films/config/params -H 'Content-type:application/json'  -d '{
"set" : {
  "browse": {
    "hl":"on",
    "hl.fl":"name"
    }
  }
}'
try http://localhost:8983/solr/films/browse?q=batman now, and you'll see "batman" highlighted in the results
