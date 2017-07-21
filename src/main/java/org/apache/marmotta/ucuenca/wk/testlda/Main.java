/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.testlda;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequenceWithBigrams;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import info.debatty.java.stringsimilarity.Cosine;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Main m = new Main();
        List<String> topics = m.findTopics(m.documents, 5, 10);
        m.printWeightedWords(m.getWeightedSubjects(m.subjects, topics));


//        String end = "http://190.15.141.66:8899/ucuenca/";
//        String obj = "http://190.15.141.66:8899/ucuenca/contribuyente/SAQUICELA_GALARZA__VICTOR_HUGO";
//        System.out.println(obj.matches(end + "(.*)"));
//        Matcher mm = Pattern
//                .compile("^https?:\\/\\/scholar\\.google\\.com\\/citations\\?user=.*&hl=en&oe=ASCII")
//                .matcher("http://scholar.google.com/citations?user=este es mi usuario&hl=en&oe=ASCII");
//        System.out.println(mm.matches());

    }

    private List<String> findTopics(List<String> documents, int numTopics, int numWords) throws IOException {
        Set<String> topics = new TreeSet<>();

        File stoplist = new File(getClass().getClassLoader().getResource("stoplist.txt").getFile());
        ArrayIterator iterator = new ArrayIterator(documents);

        ArrayList<Pipe> workflow = new ArrayList<>();
        workflow.add(new CharSequence2TokenSequence("\\p{L}+"));
        workflow.add(new TokenSequenceLowercase());
        workflow.add(new TokenSequenceRemoveStopwords(stoplist, "UTF-8", false, false, false));
        workflow.add(new TokenSequence2FeatureSequenceWithBigrams());

        InstanceList data = new InstanceList(new SerialPipes(workflow));
        data.addThruPipe(iterator);

        ParallelTopicModel lda = new ParallelTopicModel(numTopics);
        lda.addInstances(data);
        lda.estimate();

        for (Object[] words : lda.getTopWords(numWords)) {
            for (Object word : words) {
                topics.add(String.valueOf(word));
            }
        }
        Set<String> top = new HashSet<>();
        int i = 0;
        for (Object[] words : lda.getTopWords(2)) {
            for (Object word : words) {
                i++;
                top.add(String.valueOf(word));
                System.out.println("topico " + i + ": " + word);
            }
        }

        for (String string : top) {
            System.out.println(string);
        }
        return new ArrayList<>(topics);
    }

    private void printWeightedWords(HashMap<String, Integer> weightedWords) {
        int num_words = 1;
        for (Map.Entry<String, Integer> entry : weightedWords.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println(String.format("%d) %s:%d", num_words, entry.getKey(), entry.getValue()));
                num_words++;
            }
        }
    }

    private HashMap<String, Integer> getWeightedSubjects(List<String> subjects, List<String> topics) {
        HashMap<String, Integer> result = new HashMap<>();
        for (String subject : subjects) {
            result.put(subject.toLowerCase(), 0);
        }
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            String subject = entry.getKey();
            for (String topic : topics) {
                if (areSimilar(subject, topic)) {
                    result.put(subject, entry.getValue() + 1);
                }
            }
        }
        return result;
    }

    private boolean areSimilar(String subject, String topic) {
        Cosine l = new Cosine();
        for (String s : subject.split(" ")) {
            if (l.distance(s, topic) <= 0.1) {
                return true;
            }
        }
        return false;
    }
    // Documents
////    <editor-fold defaultstate="collapsed" desc="Verdugo">
////    http://localhost:8080/resource/authors/MOLINA_VERDUGO__ARMANDO
//    List<String> documents = Arrays.asList(DOC_A, DOC_B, DOC_C);
//    private static final String DOC_A = "Levantamiento y evaluación de aptitudes de suelo para irrigación en la Zona de Bulán- San CristobalSe realizó el levantamiento de suelos de ocho comunidades siendo el área más o menos de 2.500has. Luego se elaboró mapas de suelos con la ayuda del programa de computación como son los sistemas de información geográfica. La parte final de la tesis es la evaluación de suelos tomando en cuenta factores físicos, químicos e hidrofísicos. El producto final de los estudios anteriores es el área regable que es el área de suelo apta para irrigación";
//    private static final String DOC_B = "This  thesis  aims  to  study  the  effect  of  vegetation  cover  and  anthropogenic activities on soil formation in a páramo micro-catchment in the upper part of the Yanuncay basin (Cuevas stream). Non-allophonic Andosols are the main soils in  the  study  area  where  the  overgrazing  and  burnings  are  the  common agricultural practices.\n"
//            + "Three types of vegetation were identified, grass páramo (Pj), cushion páramo (PHA), and Polylepis (BP) forest. 6 soil catenas were made and 18 soil profiles were studied for the distinct types of vegetation. Results of the statistical analyses show that the availability of cations and Si is low, whereas the availability of Al and Fe is high and these elements are strongly correlated to organic carbon. The upper horizons present values of pH lower to 5, bulk density is lower to 0.9gr/cm³, and the values of the ratio of Alp/Alo are between 0.5 and 1. These findings  indicate  that  the  páramo  soils  of  the  southern  Ecuador  are  non-allophonic  Andosols  and  the  alumínic-organic  complexes  prevail,  while  the formation of allophones is limited, therefore these soils are well developed.\n"
//            + "Our  analysis  show  that  there  are  two  patterns  between  vegetation  groups, apparently the grass páramo and Polylepis forest have the same behavior, as they do not show significative differences. On the other hand, the results for the cushion  páramo  show  a  distinct  behavior  in  comparison  to  the  other  two groups.  Land  use  plays  an  indirect  role  but  important  in  soil  development, particularly  the  Pj  and  Pha  are  subjected  to  overgrazing  of  animals  and burnings,  these  agricultural  practices  contribute  further  to  disturb  the biogeochemical cycle of páramo ecosystems";
//    private static final String DOC_C = "Impacto de la cobertura vegetal y las actividades antrópicas sobre la formación del suelo en una microcuenca de páramo en la cuenca del Río Yanuncay-Quebrada Cuevas Esta tesis tiene por objetivo estudiar el  efecto de la cobertura vegetal  y las actividades antrópicas sobre la formación del  suelo en una Microcuenca de páramo en la Cuenca alta del río Yanuncay – quebrada Cuevas. Los Andosoles no alofánicos son los suelos que predominan en el área de estudio donde el sobrepastoreo y las quemas son las prácticas agrícolas más comunes.\n"
//            + "Tres tipos de vegetación se identificaron, el páramo de pajonal (Pj), páramo de almohadillas (Pha) y el bosque de Polylepis (Bp). Se realizaron 6 catenas desuelo  y  se  estudiaron  18  perfiles  de  suelo  para  los  diferentes  tipos  de vegetación. Los Resultados  de  los  análisis  estadísticos  muestran  que  la disponibilidad de los cationes y Si es baja mientras que la disponibilidad del Al y Fe es alta y estos elementos están bien correlacionados con la cantidad de carbón orgánico. Los horizontes superficiales presentan valores de pH menores a 5, la D.A es menor a 0.9 gr/cm³, y los valores de la proporción de Alp/Alo se encuentran entre 0.5 y 1. Estos resultados indican que los suelos del páramo del sur del Ecuador son Andosoles no alofánicos y predominan los complejos alumino-orgánicos mientras que la formación de alófanas es muy débil o simplemente no existe, por lo tanto son suelos bien desarrollados.\n"
//            + "Nuestros  análisis muestran  que  existen  dos  patrones  entre  los  grupos  de vegetación, aparentemente el páramo de pajonal y el bosque de Polylepis tiene el  mismo  comportamiento  ya  que  no  tienen  diferencias  significativas.  En cambio los resultados para el páramo herbáceo con almohadillas muestran un comportamiento distinto que difiere de los otros dos grupos. El uso del suelo juega  un  papel  indirecto  pero  importante  en  el  desarrollo  del  suelo";
//    private static final List<String> subjects = Arrays.asList("PROPIEDADES QUIMICA DEL SUELO",
//            "MICROCUENCA DE PARAMO",
//            "IMPACTO DE COBERTURA VEGETAL",
//            "CUENCA ALTA DEL RIO YANUNCAY",
//            "CATENAS DE SUELO",
//            "ANDASOL NO ALOFANICO",
//            "PROPIEDADES FISICAS DEL SUELO",
//            "SUELOS SISTEMAS RIEGO",
//            "LEVANTAMIENTO EVALUACION SUELOS",
//            "BULAN",
//            "PARAMO");
////</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Saquicela">
    //http://localhost:8080/meta/text/html/authors/SAQUICELA_GALARZA__VICTOR_HUGO
    List<String> documents = Arrays.asList(DOC_A, DOC_B, DOC_C, DOC_D, DOC_E, DOC_F, DOC_G, DOC_H, DOC_I, DOC_J, DOC_K, DOC_L, DOC_M, DOC_N, DOC_O);
    List<String> documents_es = Arrays.asList(DOC_A, DOC_C, DOC_E, DOC_G, DOC_I, DOC_K, DOC_M, DOC_O);
    List<String> documents_en = Arrays.asList(DOC_B, DOC_D, DOC_F, DOC_H, DOC_J, DOC_L, DOC_N);
    List<String> subjects = Arrays.asList("ONLINE PROCESO ANALITICO",
            "INFORMATICA",
            "TRANSFERENCIA DE ARCHIVOS",
            "POSGRES",
            "FI WALLS",
            "ACCESO A INTERNET",
            "SELECCION DE ONTOLOGIAS",
            "PLATAFORMA DE ANOTACION",
            "BUS DE SERVICIOS",
            "ANOTACION SEMANTICA",
            "SCRAPING",
            "PROGRAMACION ELECTRONICA",
            "GUIA DE PROGRAMACION",
            "EXTRACCION DE INFORMACION",
            "DBPEDIA",
            "TOMCAT",
            "JSP",
            "WEBSEMANTICA",
            "LINKED DATA",
            "GEOSPARQL",
            "GEOESPACIAL",
            "GEO LINKED DATA",
            "DATOS GEOESPACIALES",
            "SISTEMA DE SOPORTE A DECISIONES",
            "OLAP",
            "HEFESTO",
            "WEKA",
            "SISTEMA DE SOPORTE",
            "PENTAHO",
            "CENTRO DE DOCUMENTACION JUAN BAUTISTA VAZQUEZ",
            "BIBLIOTECA JUAN BAUTISTA VAZQUEZ",
            "BIBLIOMINING",
            "ONTOLOGIA",
            "PENTAHO DATA INTEGRATION",
            "PDI",
            "LOD",
            "LINKED OPEN DATA",
            "JAVA",
            "WEB SEMANTICA",
            "DATOS ENLAZADOS",
            "SERVICIOS WEB",
            "DATAWAREHOUSE",
            "TELEVISION DIGITAL",
            "CORREO ELECTRONICO",
            "DATA WAREHOUSE",
            "AUTOMATIZACION",
            "INTERNET",
            "LINUX",
            "SPARQL",
            "RDF");

    //Spanish
    private static final String DOC_A = "Enriquecimiento semántico de guías de programación electrónica Los grandes avances en el ámbito tecnológico y especialmente en el campo de la Televisión Digital han generado nuevas líneas de investigación, y esta tesis es un ejemplo de ello. Este trabajo presenta una forma de ayudar a los usuarios de televisión digital a obtener información de forma rápida sobre la programación que observan y es de su interés. Sin embargo, el problema actual se debe a que los difusores de televisión digital no siempre aportan la información suficiente de la programación a sus usuarios, lo que ocasiona que los usuarios recurran a otras fuentes de información perdiendo tiempo valioso. Este trabajo describe una solución a este problema al integrar la televisión y la gran cantidad de información disponible en internet mediante el uso de tecnologías semánticas con el fin de que los televidentes obtengan información completa de los programas que ellos desean conocer. Específicamente esta tesis busca enriquecer o suministrar información adicional a los programas transmitidos por los diferentes canales de televisión utilizando métodos y técnicas del campo de la web semántica con el objetivo de recopilar información útil desde repositorios ontológicos y no ontológicos. El resultado final será una base de conocimiento que contenga información detallada de la programación que emite un canal de televisión digital.";
    private static final String DOC_C = "Diseño e implementación de un repositorio ecuatoriano de datos enlazados geoespaciales Este trabajo presenta los procesos y actividades realizadas para la generación, publicación y visualización de Datos Enlazados Geoespaciales (Geo Linked Data) del Ecuador ya que el avance de la Web se enfoca en la publicación de este tipo de datos, permitiendo que estén estructurados de tal manera que se puedan interconectar entre y desde diferentes fuentes. Por lo tanto, para generar estos Datos Enlazados Geoespaciales, se procedió a la ejecución de cada uno de los pasos de la metodología de publicación de Datos Enlazados. Sin embargo, existieron problemas que dificultaron esta actividad, ya que es necesario el conocimiento de tecnologías semánticas que permitan llevar a cabo la generación de este tipo de información. Entonces, este trabajo describe una solución que permita la fácil generación de Datos Enlazados Geoespaciales, para lo cual, con la ayuda de buscadores Web, se inició con la recolección manual de datos con su respectiva información Geoespacial, después se procedió a desarrollar herramientas que faciliten la generación de Datos Enlazados Geoespaciales y a modificar otras previamente desarrolladas con el fin de dar compatibilidad al consumo del nuevo tipo de información y, finalmente, con la utilización de estas herramientas dentro de la metodología de publicación, realizar la implementación del prototipo de repositorio Ecuatoriano de Datos Enlazados Geoespaciales.";
    private static final String DOC_E = "Data warehouse para el Centro de Documentación Regional \"Juan Bautista Vázquez\" El proceso de toma de decisiones en las bibliotecas universitarias es de suma importancia, sin embargo, se encuentra complicaciones como la gran cantidad de fuentes de datos y los grandes volúmenes de datos a analizar. Las bibliotecas universitarias están acostumbradas a producir y recopilar una gran cantidad de información sobre sus datos y servicios. Las fuentes de datos comunes son el resultado de sistemas internos, portales y catálogos en línea, evaluaciones de calidad y encuestas. Desafortunadamente estas fuentes de datos sólo se utilizan parcialmente para la toma de decisiones debido a la amplia variedad de formatos y estándares, así como la falta de métodos eficientes y herramientas de integración. Este proyecto de tesis presenta el análisis, diseño e implementación del Data Warehouse, que es un sistema integrado de toma de decisiones para el Centro de Documentación Juan Bautista Vázquez. En primer lugar se presenta los requerimientos y el análisis de los datos en base a una metodología, esta metodología incorpora elementos claves incluyendo el análisis de procesos, la calidad estimada, la información relevante y la interacción con el usuario que influyen en una decisión bibliotecaria. A continuación, se propone la arquitectura y el diseño del Data Warehouse y su respectiva implementación la misma que soporta la integración, procesamiento y el almacenamiento de datos. Finalmente los datos almacenados se analizan a través de herramientas de procesamiento analítico y la aplicación de técnicas de Bibliomining ayudando a los administradores del centro de documentación a tomar decisiones óptimas sobre sus recursos y servicios.";
    private static final String DOC_G = "Creación de componentes para el framework de generación de resource description framework (RDF) El proceso de generación de Datos Abiertos Enlazados (Linked Open Data - LOD en inglés) es tedioso, esto debido a la necesidad de poseer conocimientos en diferentes áreas. Actualmente, existen muchas herramientas que permiten convertir los datos disponibles en Internet a formato RDF, pero ninguna provee una manera intuitiva, integrada y ágil de realizar este proceso. En esta tesis se presenta una plataforma para la generación de LOD, que cubre los pasos de una metodología utilizada por la comunidad científica. La unificación de metodología y plataforma se realiza mediante la creación de componentes dentro de la herramienta Pentaho Data Integration (PDI). Estos componentes permiten extraer datos desde repositorios OAI-PMH, cargar las ontologías, crear mapeo entre los datos y ontologías, generar RDF, publicar un SPARQL Endpoint y explotarlo.";
    private static final String DOC_I = "Plataforma para la anotación semántica de servicio web RESTful sobre un bus de servicios Hoy en día, los Servicios Web son ampliamente usados en un gran número de aplicaciones en la Web. La mayoría de dichas aplicaciones son servicios de información sobre: clima, deportes, noticias entre otras, que se basan en el uso de la arquitectura REST. Debido a la masificación que los Servicios Web han alcanzado, es evidente la necesidad de mecanismos más sofisticados para su gestión y explotación, por lo tanto, actividades como descubrimiento, búsqueda y composición de Servicios Web han llegado a ser tareas cotidianas entre los desarrolladores. Sin embargo, la implementación de estas tareas trae algunos problemas causados por la falta de automatización de sus procesos, los requieren de una alta intervención humana.\n"
            + "Actualmente, el principal enfoque para solucionar los problemas causados por la falta de automatización en las tareas relacionadas con la gestión de Servicios Web es la incorporación de anotaciones semánticas. Este enfoque tiene por objetivo facilitar a los computadores a interpretar y entender la información de los Servicios Web, para que así las tareas antes mencionadas (descubrimiento, búsqueda y composición) puedan ser automatizadas. No obstante, la mayoría de los procesos de anotación propuestos hoy aún requieren de procedimientos manuales. En este contexto, la presente tesis describe el desarrollo de una plataforma para anotación semántica automática de Servicios Web, plataforma que permite la generación de anotaciones semánticas con una reducida intervención humana, a través del desarrollo de un componente permite la selección automática de ontología de acuerdo al dominio de los Servicios Web.";
    private static final String DOC_K = "Diseño e implementación de un sistema de soporte de decisiones para el Centro de Documentación Regional “Juan Bautista Vázquez” El volumen de datos en bibliotecas ha aumentado enormemente en los últimos años, así como también la complejidad de sus fuentes y formatos de información, dificultando su gestión y acceso, especialmente como apoyo en la toma de decisiones. Sabiendo que una buena gestión de bibliotecas involucra la integración de indicadores estratégicos, la implementación de un Data Warehouse (DW), que gestione adecuadamente tal cantidad de información, así como su compleja mezcla de fuentes de datos, se convierte en una alternativa interesante a considerar. El artículo describe el diseño e implementación de un sistema de soporte de decisiones (DSS) basado en técnicas de DW para la biblioteca de la Universidad de Cuenca. Para esto, el estudio utiliza una metodología holística, propuesto por Siguenza-Guzman et al. (2014) para la evaluación integral de bibliotecas. Dicha metodología evalúa la colección y los servicios, incorporando importantes elementos para la gestión de bibliotecas, tales como: el desempeño de los servicios, el control de calidad, el uso de la colección y la interacción con el usuario. A partir de este análisis, se propone una arquitectura de DW que integra, procesa y almacena los datos. Finalmente, estos datos almacenados son analizados y visualizados a través de herramientas de procesamiento analítico en línea (OLAP). Las pruebas iniciales de implementación confirman la viabilidad y eficacia del enfoque propuesto, al integrar con éxito múltiples y heterogéneas fuentes y formatos de datos, facilitando que los directores de bibliotecas generen informes personalizados, e incluso permitiendo madurar los procesos transaccionales que diariamente se llevan a cabo.";
    private static final String DOC_M = "Maskana. Revista Científica El volumen de datos en bibliotecas ha aumentado enormemente en los últimos años, así como también la complejidad de sus fuentes y formatos de información, dificultando su gestión y acceso, especialmente como apoyo en la toma de decisiones. Sabiendo que una buena gestión de bibliotecas involucra la integración de indicadores estratégicos, la implementación de un Data Warehouse (DW), que gestione adecuadamente tal cantidad de información, así como su compleja mezcla de fuentes de datos, se convierte en una alternativa interesante a considerar. El artículo describe el diseño e implementación de un sistema de soporte de decisiones (DSS) basado en técnicas de DW para la biblioteca de la Universidad de Cuenca. Para esto, el estudio utiliza una metodología holística, propuesto por Siguenza-Guzman et al. (2014) para la evaluación integral de bibliotecas. Dicha metodología evalúa la colección y los servicios, incorporando importantes elementos para la gestión de bibliotecas, tales como: el desempeño de los servicios, el control de calidad, el uso de la colección y la interacción con el usuario. A partir de este análisis, se propone una arquitectura de DW que integra, procesa y almacena los datos. Finalmente, estos datos almacenados son analizados y visualizados a través de herramientas de procesamiento analítico en línea (OLAP). Las pruebas iniciales de implementación confirman la viabilidad y eficacia del enfoque propuesto, al integrar con éxito múltiples y heterogéneas fuentes y formatos de datos, facilitando que los directores de bibliotecas generen informes personalizados, e incluso permitiendo madurar los procesos transaccionales que diariamente se llevan a cabo.";
    private static final String DOC_O = "Sofware para regular el manejo del correo electrónico, control de acceso a internet y la transferencia de archivos en una red corporativa Optimización del uso de los recursos de internet, mediante manejo del correo electrónico en el web, logrando que los usuarios de una red corporativa puedan revisar su correo desde cualquier parte del mundo sin necesidad de configuración alguna. Control de acceso a internet a través de las asignaciones. Acelerar la transferencia de archivos con la configuración de servidores proxy.";

    //English
    private static final String DOC_B = ""
            //+ "Enriquecimiento semántico de guías de programación electrónica "
            + "The great developments in the technological area and especially in the area of the Digital Television have generated new researching lines, and this thesis is an example of that. This work presents a helping tool for Digital Television users providing the fastest way to get information about the programs they observe and which they like. However, the actual problem occurs since the Digital Television broadcasters not always give enough information to users about the programs, which is a problem because they use other information sources wasting worthy time. A solution to that problem is developed in this work, since the television and the huge and available information in the internet are integrated by using semantic technologies in order to let users get a complete information about the programs they want to know. Especially this thesis looks for enriching extra information about the programs transmitted by the different television channels by using the semantic web methods and techniques to get helpful information from ontological and no ontological repositories. The final result will be an ontological data base containing detailed program information.";
    private static final String DOC_D = ""
            //+ "Diseño e implementación de un repositorio ecuatoriano de datos enlazados geoespaciales "
            + "This work presents the processes and activities done for generation, publication and visualization of Geospatial Linked Data of Ecuador, considering that the Web advance (in both, Europe and North America) is focused on publication of this data types and let it to be structured and interconnected, to and from, different sources. Therefore, to generate Geospatial Linked Data, proceeded with the execution of every step of Linked Data's publication methodology. However, there were some troubles for this activity since semantic technologies knowledge is required to implement the generation process. Then, this work describes one solution to allow an easy Geospatial Linked Data generation, for which with a Web search, began a manual recollection of data that contains Geospatial information; after, proceeded with the development of new tools and the modification of the old ones for helping the Geospatial Linked Data generation and adding compatibility of new format of information; and finally, with the use of the developed and modified tools, implement and publish the prototype of the Ecuadorian's Geospatial Linked Data repository.";
    private static final String DOC_F = ""
            //+ "Data warehouse para el Centro de Documentación Regional \"Juan Bautista Vázquez\" "
            + "The decision-making process in academic libraries is paramount; however highlycomplicated due to the large number of data sources, processes and high volumes ofdata to be analyzed. Academic libraries are accustomed to producing and gatheringa vast amount of statistics about their collection and services. Typical data sourcesinclude integrated library systems, library portals and online catalogues, and systemsof consortiums and quality surveys. Unfortunately, these heterogeneous datasources are only partially used for decision-making processes due to the wide varietyof formats, standards and technologies, as well as the lack of efficient methods ofintegration. This thesis presents the analysis, design and implement of Data Warehousefor an academic library \\Juan Bautista Vázquez\". Firstly, an appropriatemethodology documented in a previous study is used for data collection. This methodology incorporates key elements including process analysis, quality estimation, information relevance and user interaction that may influence a library decision. Based on the above mentioned approach, this study defines a set of queries of interestto be issued against the integrated system proposed. Then, relevant data sources,formats and connectivity requirements for a particular example are identified. Next,Data Warehouse architecture is proposed to integrate, process, and store the collecteddata transparently. Eventually, the stored data are analyzed through reportingtechniques of analytical processing and prototype of Bibliomining. By doing so, thethesis provides the design of an integrated solution that assists library managers tomake tactical decisions about the optimal use and leverage of their resources and of services.";
    private static final String DOC_H = ""
            //+ "Creación de componentes para el framework de generación de resource description framework (RDF) "
            + "The generation of Linked Open Data (LOD) is an overwhelming process, since a vast knowledge in several fields is needed. Currently, there are several tools that allow converting Internet available data into the RDF format, however, none of them provides an intuitive, integrated or suitable way to perform the conversion process. This thesis work presents a LOD generation platform that involves the corresponding steps related to a well-accepted scientific community methodology. The fusion of the proposed platform and such methodology is performed by creating components withing Pentaho Data Integration (PDI), that allows: data extraction from OAI-PMH repositories, data ontology mapping, RDF generation and, SPARQL Endpoint publication and exploiting.";
    private static final String DOC_J = ""
            + "Nowadays, Web Services are widely used so that there is an immense collection of applications on the Web. Most of such applications, like information services about: weather, sports, news, among and others, rely on the use the REST architecture.  Due to the massification that Web Services have reached, it is evident that more sophisticated mechanisms for managing and exploitation them are needed therefore, activities of web services such as discovery, searching and composition have become daily tasks among developers. However, some tasks implementation brings some obstacles caused by the lack of automatization within its processes, which requires high human intervention.\n"
            + "Currently, the main approach in solving the problem proposed by the lack of automation tasks related to the management of Web Services corresponds to the incorporation of semantic annotations.  This approach aims to help computers to interpret and understand the information of Web Services, so that the aforementioned tasks (discovery, searching and composition) can be automated. Nonetheless, most of semantic annotations approaches require manual procedures up to date. Within this context, this thesis work describes the development of a platform for  Web Services automatic semantic annotation, a platform that allows generating semantic annotations with reduced human intervention through the development of a component that allows the automatic selection of ontologies according to the Web Services domain.";
    private static final String DOC_L = ""
            //+ "Diseño e implementación de un sistema de soporte de decisiones para el Centro de Documentación Regional “Juan Bautista Vázquez” "
            + "In recent years, volume of library-related data has increased tremendously, as well as complexity of data sources and formats have been escalating. This information explosion has created a big challenge for data managing, archiving and accessing, especially in support of library decision-making. Knowing that a good library management involves integrating a number of strategic indicators, the implementation of a Data Warehouse (DW), that properly manages such amount of information and the complex mix of data sources, becomes an interesting alternative to be considered. This article describes the design and implementation of a decision support system based on a DW approach for the Regional Documentation Centre “Juan Bautista Vazquez”. To assure that all relevant data sources are included during the data analysis, the study utilizes a holistic methodology, previously proposed by Siguenza-Guzman et al. (2014) for an integrated library evaluation. This methodology assesses the library collection and services by incorporating important elements for library management, such as service performance analysis, service quality control, collection usage analysis, and information retrieval quality. Based on this data analysis, the study proposes a DW architecture to integrate, process and store the relevant data. These stored data are finally analyzed and visualized by the so-called online analytical processing (OLAP) tools. Initial tests of the implemented decision support system confirm the feasibility and effectiveness of the DW based approach, by successfully integrating multiple and heterogeneous sources, formats and varieties of information, permitting library managers to generate personalized reports, and even allowing to debug the day-to-day transactional processes.";
    private static final String DOC_N = ""
            //+ "Maskana. Revista Científica "
            + "In recent years, volume of library-related data has increased tremendously, as well as complexity of data sources and formats have been escalating. This information explosion has created a big challenge for data managing, archiving and accessing, especially in support of library decision-making. Knowing that a good library management involves integrating a number of strategic indicators, the implementation of a Data Warehouse (DW), that properly manages such amount of information and the complex mix of data sources, becomes an interesting alternative to be considered. This article describes the design and implementation of a decision support system based on a DW approach for the Regional Documentation Centre “Juan Bautista Vazquez”. To assure that all relevant data sources are included during the data analysis, the study utilizes a holistic methodology, previously proposed by Siguenza-Guzman et al. (2014) for an integrated library evaluation. This methodology assesses the library collection and services by incorporating important elements for library management, such as service performance analysis, service quality control, collection usage analysis, and information retrieval quality. Based on this data analysis, the study proposes a DW architecture to integrate, process and store the relevant data. These stored data are finally analyzed and visualized by the so-called online analytical processing (OLAP) tools. Initial tests of the implemented decision support system confirm the feasibility and effectiveness of the DW based approach, by successfully integrating multiple and heterogeneous sources, formats and varieties of information, permitting library managers to generate personalized reports, and even allowing to debug the day-to-day transactional processes.";
//</editor-fold>
//http:////<editor-fold defaultstate="collapsed" desc="Carvallo Vega">
//    //http://localhost:8080/resource/authors/CARVALLO_VEGA__JUAN_PABLO
//    List<String> subjects = Arrays.asList("EN GERENCIA DE SISTEMAS DE INFORMACION",
//            "INFORMATICA",
//            "GERENCIA DE SISTEMAS DE INFORMACION",
//            "HYBRID",
//            "CORPORACION AEROPUERTARIA CUENCA",
//            "STANDARS DE CALIDAD",
//            "GERENCIA EN SISTEMAS",
//            "HIROPAUTE",
//            "SOFTWARE EMPRESARIAL",
//            "METODO DHARMA",
//            "SISTEMA DE CONTROL INDUSTRIAL",
//            "INGENIERIA DE SOFTWARE",
//            "ELECTRONICA DIGITAL",
//            "TRANSICION DEL SERVICIO",
//            "SERVIANDINA",
//            "MATRIZ GUIA",
//            "ITIL",
//            "GRUPO CONSENSO",
//            "ESTRATEGIAS DEL SERVICIO",
//            "GERENCIA DE SISTEMAS DE INFORMACION",
//            "CORPORACION AEROPORTUARIA DE CUENCA",
//            "MICROPROCESADORES",
//            "SISTEMAS DE INFORMACION",
//            "DHARMA",
//            "PLANIFICACION ESTRATEGICA",
//            "ARQUITECTURA DE COMPUTADORAS",
//            "SOFTWARE",
//            "TICS",
//            "INFORMATICA",
//            "SISTEMAS");
//
//    List<String> documents = Arrays.asList(DOC_A, DOC_B, DOC_C, DOC_D, DOC_E, DOC_F, DOC_G, DOC_H, DOC_I, DOC_J, DOC_K);
//
//    private static final String DOC_A = "Sistema de control industrial por medio de un computador Esta tesis versa sobre el control de circuitos industriales típicos, valiéndose de un computador personal. Permite definir mapas de memoria [direcciones de entrada/salida], esquemas de control a través de módulos funcionales [Lógicos, Contadores, Temporizadores, Comparadores], por medio de una interfaz gráfica muy amigable, que además reutiliza estos esquemas como bloques guardados en librerías, para luego generar un archivo que contiene el programa de control con la programación de cada uno de los elementos colocados en el esquema; logrando de esta manera realizar el control de elementos externos a través de los puertos del computador personal o tarjetas de expansión.";
//    private static final String DOC_B = "Mandos industriales por medio de un microprocesador Z-80 Presenta una introducción a los componenetes básicos de un microprocesador y una visión a la arquitectura más generalizada de una microcomputadora; estudia el microprocesador Z-80 de ZILOG y su familia de componenetes. Concluye con un ejemplo del sistema didáctico, el cual puede utilizarse como ayuda para generar programas de control industrial";
//    private static final String DOC_C = "Análisis de modelos de negocio y obtención de patrones arquitectónicos que sirvan de apoyo al proceso de selección de sistemas de información en las empresas Debido a las dificultades que tienen actualmente las empresas para seleccionar los sistemas de información adecuados para satisfacer sus necesidades, se proponen patrones arquitectónicos que sirvan de guía o apoyo durante este proceso de selección. Estos patrones se desarrollaron para cuatro tipos de empresas en base a su modelo de negocio: Enterprise Resource Planning Planeación de Recursos empresariales (ERP): Empresas manufactureras Customer Relationship Management Administración de la relación con los clientes (CRM): Empresas que ofrecen servicios a sus clientes Suply Chain Management Administración de la cadena de suministros (SCM): Empresas que ofrecen transporte y entrega de paquetes, insumos, etc. Selling Chain Management - Administración de la cadena de ventas (SECM): Empresas que ofrecen productos a sus clientes a través de canales de distribución. Lo que se pretende es analizar estos tipos de organizaciones desde un punto de vista de su entorno estratégico, identificando los actores en el mismo y las dependencias estratégicas existentes entre ellos y la organización Las dependencias identificadas serán analizadas para determinar cuáles pueden ser solventadas por sistemas de información, y éstos a la vez descompuestos en objetivos específicos que representan los servicios que deben ser provistos por el sistema. Los servicios identificados serán agrupados en componentes atómicos, que a la vez constituirán arquitecturas genéricas (patrones arquitectónicos), de los sistemas requeridos por los diversos tipos de organización, obteniendo de esta manera la identificación de los sistemas de información adecuados para cada tipo de empresa.";
//    private static final String DOC_D = "Plan de mejora de procesos del área de mantenimiento y optimización de sistemas de Hidropaute basado en Cmmi nivel 2 El presente trabajo busca el fortalecimiento del Área de Mantenimiento y Optimización de Sistemas de Hidropaute, mediante la planificación de la mejora de procesos. Esta planificación se basa en el modelo de mejora organizacional IDEAL, la adopción de las áreas de proceso y prácticas incluidas en el modelo integrado de capacidad y madurez CMMI y las mejores prácticas de la guía para la aplicación de ISO 9001:2000 al software de computador ISO 9000-3. En el capítulo introductorio se presentan algunos conceptos necesarios, antecedentes, problemas a solucionar, el alcance y los objetivos del trabajo. Un segundo capítulo de estado del arte ubica los modelos o estándares utilizados dentro de otras opciones del mismo tipo a nivel mundial. El tercer capítulo presenta un estudio comparativo entre CMMI nivel 2 y la norma ISO 9000-3; al inicio de dicho capítulo se realiza una breve explicación de cada uno de los modelos y a continuación se presentan tablas comparativas a diferentes niveles de abstracción. En el cuarto capítulo se desarrolla el plan de mejoramiento de procesos de software (SPI-Software Process Improvement) y la estructura documental de las áreas del primer ciclo de mejora: Gestión de Configuración (CM) y Gestión de Requerimientos (REQM); además se ha elaborado un Manual de Calidad del Software de acuerdo a los estándares de documentación de Hidropaute, el mismo que integra cada una de las normativas por área y sus procedimientos documentados.";
//    private static final String DOC_E = "Estudio comparativo de los modelos de calidad y marcos de referencia a fin de obtener atributos de calidad dirigidos a cada etapa de ciclo de vida del software La presente investigación, Estudio comparativo de modelos, estándares y marcos de referencia a fin de obtener atributos de calidad dirigidos a cada etapa del ciclo de vida del software, se enfocó en la comparación y análisis entre el modelo CMMI, el estándar 12207, el estándar 90003 y el marco de referencia COBIT, estudiando a detalle los procesos de la ingeniería del software, con el objetivo principal de obtener el listado de atributos de calidad a nivel del proceso. La investigación realizada sigue los siguientes pasos para cumplir con su propósito. En primera instancia se identificó y describió las etapas del ciclo de vida del software, analizando el estándar 12207, donde interviene los procesos de adquisición, desarrollo y servicios del software. La segunda instancia, define los conceptos de calidad orientados a la mejora continua a los procesos de software analizando el estándar 90003, el marco de referencia COBIT y el modelo de capacidad y madurez CMMI. Utilizando los conceptos y análisis mencionados se documenta la parte principal del proyecto de tesis, donde se plantean varios niveles de comparación utilizando el método normativo y descriptivo. El resultado de la comparación entre los estándares, modelos y marcos de referencia es proponer una metodología para obtener los atributos de calidad y una matriz para evaluar el desempeño de los atributos a aplicados a casos prácticos. Palabras claves: Calidad, software, método comparativo, standars de calidad, modelos de calidad, modelos del ciclo de vida de software";
//    private static final String DOC_F = "Planificación estratégica para el desarrollo de las tecnologías de información y comunicación en la Corporación Aeroportuaria de la Ciudad de Cuenca. Proyectos de desarrollo de software El Plan Estratégico de TIC s para la Corporación Aeroportuaria de Cuenca se realizó mediante un análisis del entorno, utilizando el método FODA y el modelo DHARMA, que permitieron determinar actores, dependencias y elementos necesarios para identificar los componentes informáticos requeridos por la Corporación. Estos se plasman en proyectos dentro de las áreas de Desarrollo, Infraestructura / Software Base y Adquisición. El Plan Estratégico de TICs incorpora el marco estratégico, prioriza la cartera de proyectos, determina procesos y políticas para orientar e incrementar el desempeño en cada una de las áreas involucradas. Propone la creación de la Unidad de Tecnología, para lo que determina su estructura organizacional, define los perfiles funcionales y configura los lineamientos de los proyectos establecidos";
//    private static final String DOC_G = "Planificación estratégica para el desarrollo de las tecnologías de la información y comunicación en la Corporación Aeroportuaria de la ciudad de Cuenca, proyectos de adquisición";
//    private static final String DOC_H = "Planificación estratégica para el desarrollo de las tecnologías de la información y comunicación en la corporación aeroportuaria de la ciudad de Cuenca El Plan Estratégico de TIC s para la Corporación Aeroportuaria de Cuenca se realizo mediante un análisis del entorno, utilizando el método FODA y el modelo DHARMA, que permitieron determinar actores, dependencias y elementos necesarios para identificar los componentes informáticos requeridos por la Corporación. Estos se plasman en proyectos dentro de las áreas de Desarrollo, Infraestructura / Software Base y Adquisición. El Plan Estratégico de TICs incorpora el marco estratégico, prioriza la cartera de proyectos, determina procesos y políticas para orientar e incrementar el desempeño en cada una de las áreas involucradas. Propone la creación de la Unidad de Tecnología, para lo que determina su estructura organizacional, define los perfiles funcionales y configura los lineamientos de los proyectos establecidos";
//    private static final String DOC_I = "Análisis, diseño e implementación de un componente de software que soporte el método Dharma La presente tesis se fundamenta en analizar, desarrollar e implementar un componente de software que brinde soporte a las distintas actividades del método DHARMA (Discovering Hybrid Architectures by Modelling Actors). La finalidad del componente a implementar, consiste en proveer a los analistas de sistemas informáticos, una serie de funcionalidades que permitan disminuir el esfuerzo, al aplicar las cuatro actividades del método DHARMA basado en modelos i* para descubrir la arquitectura de los sistemas de software híbridos. El componente estará disponible en un espacio web, de tal manera que, los analistas puedan acceder al componente desde cualquier lugar mediante una conexión a internet y un navegador web. La utilidad del componente es considerable, pues permite a los usuarios aplicar el método DHARMA a varias organizaciones en un mismo ambiente de trabajo. Los analistas pueden reusar información de las organizaciones y simplificar el trabajo al modelar una nueva organización. El componente de software también permite el trabajo grupal, de tal manera que varios analistas pueden modelar una organización al mismo tiempo. Todo esto de manera fácil y sencilla, optimizando el trabajo de los analistas de software.";
//    private static final String DOC_J = "Análisis, diseño e implementación de un componente de software que soporte el método Dharma The present study analyzes, develops and implements a software component that brings a support the different activities of DHARMA (Discovering Hybrid Architectures by Modelling Actors) method. The incorporation of this component has the finality of brings to the software analysts a function serial that let them use the four activities of DRHAMA method based in i* models in an easy way. The component will be available in a web page therefore the analysts can use it with an Internet connection and a web navigator. The utility of this component is considerable, because it lets users to apply the DHARMA method to many organizations at the same environment of work. The analysts can reuse the information of the organizations and simplify the work when they want to model a new organization. The software component also lets the group work, so many analysts can model one organization at the same time. All this in an easy and simple way of model organizations, optimizing the work of the analysts.";
//    private static final String DOC_K = "Elaboración de una matriz guía para la evaluación preparatoria del área de sistemas del grupo consenso, respecto a las mejores prácticas propuestas en la Biblioteca de infraestructura de tecnologías de información itil V.3.0 La presente propuesta de tesis se desarrolla con el objetivo de conocer el estado actual del Área de Tecnología de la empresa Serviandina S.A, con respecto a la adopción de las mejores prácticas de la Biblioteca de Infraestructura de Tecnologías de Información en su versión 3.0. El proyecto consta del desarrollo de una herramienta Matriz Guía , que a través de una evaluación permite identificar el nivel de cumplimiento o adopción que se tiene por cada etapa del ciclo de vida de un servicio con respecto a las mejores prácticas. Los resultados de la evaluación son presentados en cuadros tabulados y gráficos estadísticos que pueden ser utilizados por mandos altos, medios y operativos, facilitando la revisión del nivel de cumplimiento que se tiene tanto en forma general como en forma detallada por cada proceso, indicador y rol particular de una etapa. Para obtener la Matriz Guía , se cuenta con una metodología basada en cuatro fases: Investigación, Análisis, Desarrollo de Propuesta y Aplicación de la Propuesta y Generación de Resultados las cuales que facilitan su elaboración. La fase de Investigación comprende la realización de un análisis de la problemática actual del Área de TI y del entorno en la que se desenvuelve y se realiza además un estudio minucioso de las cinco etapas de ITIL para determinar los aspectos relevantes para la evaluación. En las fases de Análisis y Desarrollo de Propuesta, se procede a realizar la construcción de la Matriz en base a los aspectos identificados en la fase anterior, además se establece el mecanismo para el cálculo, tabulación y presentación de resultados. En la fase de Aplicación de la Propuesta y Presentación de Resultados se ejecuta la evaluación y se obtiene un diagnóstico de situación actual, acompañado del detalle de los hallazgos encontrados por cada etapa y proceso del ciclo de vida del servicio. Con este diagnóstico se emiten sugerencias de mejora y una posible ruta de acción. Finalmente presentan conclusiones y recomendaciones sobre el trabajo realizado y se sugieren líneas de trabajo futuras para posteriores investigaciones.";
////</editor-fold>
////<editor-fold defaultstate="collapsed" desc="Carvallo Ochoa">
//    //http://localhost:8080/meta/text/html/authors/CARVALLO_OCHOA__JUAN_PABLO
//    List<String> subjects = Arrays.asList("PROYECTOS ARQUITECTONICOS",
//            "ARQUITECTURA",
//            "OBREGON Y VALENZUELA",
//            "CONJUNTO BAVARIA",
//            "ARQUITECTURA COLOMBIANA",
//            "MIES VAN DER ROHE LUDWIG",
//            "ARQUITECTURA SIGLO XX",
//            "ARQUITECTURA ALEMANA",
//            "PROYECTOS ARQUITECTONICOS");
//    List<String> documents = Arrays.asList(DOC_A, DOC_B);
//    private static final String DOC_A = "Aproximación a la obra de Obregón, Valenzuela, el conjunto Bavaria 1962-1966 La investigación se concentra en el caso de un país vecino, Colombia, donde la arquitectura moderna llegó en momentos de bonanza económica, a principios de la década de 1940. Donde los nuevos retos que presentó la modernidad fueron afrontados por los jóvenes profesionales con un carácter nacional determinado por el dominio del oficio, técnicas, materiales y por un correcto entendimiento de la idiosincrasia y situación colombiana de la época. Dentro de esta producción se encuentra la realizada por la firma Obregón, Valenzuela y Cía., fundada por los arquitectos Pablo Valenzuela, Rafael Obregón González y José María Obregón Rocha. Partiendo del análisis de una obra puntual de dicha firma, se plantea comprender como este y otros despachos afrontaron la modernidad, asumiendo los criterios universales de las vanguardias europeas. Criterios que se reconocen tanto en los modelos ejemplares como en los resultados siguientes, convirtiéndose en materiales de proyecto. De esta forma en la arquitectura moderna podemos reconocer varios episodios formales, como el denominado Torre-Plataforma, que ejemplificaremos con 3 obras insignias de la arquitectura moderna. Así, el trabajo tiene un doble objetivo, por un lado identificar y analizar los diferentes elementos que conforman el episodio torre-plataforma, y por otro estudiar y confrontar estos elementos al Conjunto Bavaria. Este reconocimiento del episodio en el conjunto se lo afrontará por medio de la aproximación a la obra, exponiendo cómo el proyecto y sus partes se develan según el ángulo y distancia en la que el observador se aproxime. Dentro de esta aproximación se analizaran los diferentes elementos del episodio, la materialidad, estructura, formalidad y funcionalidad del conjunto.";
//    private static final String DOC_B = "La Mirada constructiva a través de mies van der Rohe Esta tesis tiene el objetivo de estudiar detenidamente a uno de los maestros de la arquitectura moderna, el arquitecto alemán Ludwig Mies van de Rohe, abarcando el estudio de la totalidad de su obra realizada entre Europa y américa desde inicios del siglo XX hasta su muerte en la década de los setenta de este siglo...";
////</editor-fold>
}
