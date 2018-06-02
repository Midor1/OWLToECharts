package OWLToECharts;

import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Midor on 2018/5/22.
 *
 */
public class Parser {
    private OntModel ontModel;
    private String prefixURL;
    private FileWriter json;
    public Parser() {
        try {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
            ontModel.read(new FileInputStream("F:\\Spring\\OWLToECharts\\ontology.owl"),"");
            prefixURL = "http://www.semanticweb.org/shushu/ontologies/2018/2/untitled-ontology-257#";
            json = new FileWriter("F:\\Spring\\OWLToECharts\\ontology.json", false);
        }
        catch(IOException ioe) {
            System.out.println(ioe.toString());
        }
    }
    private void jsonName(String name) {
        try {
            json.write("{\"name\":\"" + name + "\"");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void jsonChildren() {
        try {
            json.write(",\"children\": [");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void jsonNameEnd() {
        try {
            json.write("},");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void jsonNameEndAll() {
        try {
            json.write("}");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void jsonChildrenEnd() {
        try {
            json.write("]");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void End() {
        try {
            json.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private ResultSet execQuery(String string) {
        Query query = QueryFactory.create(string);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        return queryExecution.execSelect();
    }
    public void ShowStatus() {
        for (Iterator<OntClass> i = ontModel.listClasses();i.hasNext();) {
            OntClass c = i.next();
            System.out.println("ontClass:" + c.getLocalName());
        }
        for (Iterator<Individual> i = ontModel.listIndividuals(); i.hasNext();){
            Individual j = i.next();
            System.out.println("individual:" + j.getLocalName());
        }

    }
    public void Parse() {
        //First, Find all individuals that typed AbstractService and had no parents.
        //These individuals are secondary-root nodes.
        ResultSet resultSet = execQuery("PREFIX source:<" + prefixURL + "> SELECT ?individual WHERE {?individual a source:AbstractService MINUS {?all source:hasPart ?individual}}");
        jsonName("AllServices");
        Beautify(resultSet);
        jsonNameEndAll();
        End();
    }

    private void RecursivelyParse(RDFNode root) {
        //There are three parent-child relationship types, e.g. hasPart, hasInstantiation and hasComponents.
        ResultSet resultSet;
        resultSet = execQuery("PREFIX source:<" + prefixURL + "> SELECT ?individual WHERE {"
                + "{source:" + root.asResource().getLocalName() + " source:hasInstantiation ?individual} UNION "
                + "{source:" + root.asResource().getLocalName() + " source:hasPart ?individual} UNION "
                + "{source:" + root.asResource().getLocalName() + " source:hasComponents ?individual}}");
        Beautify(resultSet);
    }

    private void Beautify(ResultSet resultSet) {
        //For individuals we've got, we recursively check if they have properties like "hasInstantiation" or "hasPart" with RecursivelyParse().
        boolean child = false;
        if (resultSet.hasNext())
            child = true;
        if (child) jsonChildren();
        while (resultSet.hasNext()) {
            QuerySolution querySolution = resultSet.next();
            RDFNode node = querySolution.get("individual");
            System.out.println(node.asResource().getLocalName());
            jsonName(node.asResource().getLocalName());
            RecursivelyParse(node);
            jsonNameEnd();
        }
        if (child) jsonChildrenEnd();
    }
}
