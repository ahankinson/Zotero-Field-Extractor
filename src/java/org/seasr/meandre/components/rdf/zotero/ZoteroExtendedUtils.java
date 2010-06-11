package org.seasr.meandre.components.rdf.zotero;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public abstract class ZoteroExtendedUtils
{
  private static final String HTTP_WWW_GUTENBERG_ORG_FILES = "http://www.gutenberg.org/files/";
  private static final String HTTP_WWW_GUTENBERG_ORG_ETEXT = "http://www.gutenberg.org/etext/";

  public static List<Vector<String>> extractFields(Model model, String zoteroField, Logger logger)
  {
    logger.info("logger 1");
    String QUERY_HEADER = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>PREFIX dc: <http://purl.org/dc/elements/1.1/>PREFIX foaf: <http://xmlns.com/foaf/0.1/>PREFIX bib: <http://purl.org/net/biblio#>PREFIX dcterms: <http://purl.org/dc/terms/>PREFIX prism: <http://prismstandard.org/namespaces/1.2/basic/>PREFIX z: <http://www.zotero.org/namespaces/export#>";

    String rdfQuery = "";

    logger.info("foo");
    logger.info("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>PREFIX dc: <http://purl.org/dc/elements/1.1/>PREFIX foaf: <http://xmlns.com/foaf/0.1/>PREFIX bib: <http://purl.org/net/biblio#>PREFIX dcterms: <http://purl.org/dc/terms/>PREFIX prism: <http://prismstandard.org/namespaces/1.2/basic/>PREFIX z: <http://www.zotero.org/namespaces/export#>");

    if (zoteroField.equalsIgnoreCase("author")) {
      logger.info("processing authors");
      rdfQuery = "SELECT DISTINCT ?doc ?first ?last WHERE {?doc rdf:type rdf:Seq .?doc ?pred ?value .?author rdf:type foaf:Person .?author foaf:givenname ?first .?author foaf:surname ?last} ORDER BY ?doc";
    }
    else if (zoteroField.equalsIgnoreCase("title")) {
      rdfQuery = "SELECT DISTINCT ?doc ?value WHERE {?doc rdf:type rdf:Seq .?doc ?pred ?x .?x rdf:type bib:Article .?x dc:title ?value .} ORDER BY ?doc";
    }
    else if (zoteroField.equalsIgnoreCase("date")) {
      rdfQuery = "SELECT DISTINCT ?doc ?value WHERE {?doc rdf:type rdf:Seq .?doc ?pred ?x .?x rdf:type bib:Article .?x dc:date ?value .} ORDER BY ?doc";
    }
    else if (zoteroField == "doi") {
      rdfQuery = "";
    } else if (zoteroField == "issn") {
      rdfQuery = "";
    } else if (zoteroField.equalsIgnoreCase("volume")) {
      rdfQuery = "SELECT DISTINCT ?doc ?value WHERE {?doc rdf:type rdf:Seq .?doc ?pred ?foo . ?x rdf:type bib:Article . ?x dcterms:abstract ?value} ORDER BY ?doc";
    }
    else if (zoteroField.equalsIgnoreCase("abstract")) {
      rdfQuery = "SELECT DISTINCT ?doc ?value WHERE {?doc rdf:type rdf:Seq .?doc ?pred ?foo .?x rdf:type bib:Article .?x dcterms:abstract ?value} ORDER BY ?doc";
    }
    else if (zoteroField == "series") {
      rdfQuery = "";
    } else if (zoteroField == "itemtype") {
      rdfQuery = "";
    }

    logger.info("header");
    logger.info(QUERY_HEADER);

    logger.info("query");
    logger.info(rdfQuery);

    String FULL_QUERY = QUERY_HEADER + rdfQuery;

    logger.info("Full query");
    logger.info(FULL_QUERY);

    Query query = QueryFactory.create(FULL_QUERY);
    QueryExecution exec = QueryExecutionFactory.create(query, model, null);
    ResultSet results = exec.execSelect();

    String sLastDocID = "";
    List vecRes = new LinkedList();
    Vector vec = null;
    while (results.hasNext()) {
      QuerySolution resProps = results.nextSolution();
      String sDoc = resProps.getResource("doc").toString();
      logger.info("doc: " + sDoc);
      String sValue = "";
      if (zoteroField.equalsIgnoreCase("author")) {
        logger.info("extracting names...");
        String sFirst = resProps.getLiteral("first").getString();
        String sLast = resProps.getLiteral("last").getString();

        logger.info("First: " + sFirst);
        logger.info("Last: " + sLast);

        sValue = sLast + ", " + sFirst;
      } else {
        sValue = resProps.getLiteral("value").getString();
      }

      if (sDoc.equals(sLastDocID)) {
        logger.info("add to existing vec");
        vec.add(sValue);
      } else {
        if (vec != null) {
          logger.info("committing vec");
          vecRes.add(vec);
        }
        logger.info("New vector");
        vec = new Vector();
        vec.add(sValue);
        sLastDocID = sDoc;
      }
    }
    if (vec == null) vec = new Vector();
    if (vec.size() > 0) vecRes.add(vec);

    return vecRes;
  }

  public static Map<String, String> extractURLs(Model model)
  {
    String QUERY_TYPE_URI_TITLE = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX dc: <http://purl.org/dc/elements/1.1/>\nPREFIX foaf: <http://xmlns.com/foaf/0.1/>\nPREFIX bib: <http://purl.org/net/biblio#>\nPREFIX dcterms:  <http://purl.org/dc/terms/>\nPREFIX z:       <http://www.zotero.org/namespaces/export#> \nSELECT ?type ?uri ?title ?a ?n \nWHERE {       ?n rdf:value ?uri .       ?n rdf:type dcterms:URI .       ?a z:itemType ?type .       ?a dc:title ?title .       ?a dc:identifier ?n . } order by ?type ?uri ?title ?a ?n ";

    Query query = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX dc: <http://purl.org/dc/elements/1.1/>\nPREFIX foaf: <http://xmlns.com/foaf/0.1/>\nPREFIX bib: <http://purl.org/net/biblio#>\nPREFIX dcterms:  <http://purl.org/dc/terms/>\nPREFIX z:       <http://www.zotero.org/namespaces/export#> \nSELECT ?type ?uri ?title ?a ?n \nWHERE {       ?n rdf:value ?uri .       ?n rdf:type dcterms:URI .       ?a z:itemType ?type .       ?a dc:title ?title .       ?a dc:identifier ?n . } order by ?type ?uri ?title ?a ?n ");
    QueryExecution exec = QueryExecutionFactory.create(query, model, null);
    ResultSet results = exec.execSelect();

    Map mapURLs = new HashMap();

    while (results.hasNext()) {
      QuerySolution resProps = results.nextSolution();
      String typeValue = resProps.getLiteral("type").toString();

      if (typeValue.equalsIgnoreCase("attachment"))
      {
        continue;
      }

      String sURI = resProps.getLiteral("uri").toString();
      String sTitle = resProps.getLiteral("title").toString();
      sURI = adjustSpecialCaseURL(sURI);
      mapURLs.put(sURI, sTitle);
    }

    return mapURLs;
  }

  private static String adjustSpecialCaseURL(String sUrl)
  {
    if (sUrl.startsWith("http://www.gutenberg.org/etext/"))
    {
      String sTmp = sUrl.substring("http://www.gutenberg.org/etext/".length());
      sUrl = "http://www.gutenberg.org/files/" + sTmp + "/" + sTmp + ".txt";
    }
    return sUrl;
  }
}