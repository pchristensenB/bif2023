package com.box.pse.bif;

import com.box.sdk.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class BifExtract {


    public static void main(String[] args) throws Exception{

        if(args.length!=3) {
            System.out.println("args should be: folderId token pathToOutputFile");
            System.exit(0);
        }
        final String SAMPLE_CSV_FILE = args[2];

        String token = args[1];
        String folderId = args[0];
        //Box client setup
        final BoxAPIConnection api = new BoxAPIConnection(token);
        MetadataTemplate template = MetadataTemplate.getMetadataTemplate(api,"boxImpactFund2022Application");
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));
        //Non-metadata Headers
        List<String> headers = new ArrayList<String>();
        headers.add("Submission date");
        headers.add("Approval stage");
        headers.add("Email address");
        headers.add("Link to application");

        //Load field/key pairs from metadata template
        Map<String,String> fieldMap = new HashMap<String,String>();
        for(MetadataTemplate.Field f:template.getFields()) {
            fieldMap.put(f.getKey(),f.getDisplayName());
        }
        //Add metadata template specific headers
        headers.add(fieldMap.get("organizationName"));
        headers.add(fieldMap.get("organizationDescription"));
        headers.add(fieldMap.get("organizationWebsite"));
        headers.add(fieldMap.get("organizationCategoryChildWelfareOrCrisisResponseOEnvironment"));
        headers.add(fieldMap.get("whatRegionscountriesWillYouOperateThisProjectOutOf"));
        headers.add(fieldMap.get("totalBudgetFromLastYear").toString());
        headers.add(fieldMap.get("howDidYouHearAboutThisGrantCallForProposal").toString());

        //Load CSV printer
        String[] stringHeaders = headers.toArray(new String[headers.size()]);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(stringHeaders)
                .build();
        CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

        BoxFolder folder = new BoxFolder(api,folderId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        int i = 0;
        Set<String> applicants = new HashSet<String>();
        for(BoxItem.Info info:folder.getChildren("id,type,created_at,name,uploader_display_name,metadata.enterprise_27335.boxImpactFund2022Application")) {
            //Only process files and only one per applicant
            if(info.getType().equals("file") && !applicants.contains(((BoxFile.Info) info).getUploaderDisplayName()) ) {

               BoxFile.Info file = (BoxFile.Info) info;
               Metadata md = file.getMetadata("boxImpactFund2022Application", "enterprise_27335");
               if(md!=null) {
                   List<String> list = new ArrayList<String>();
                   list.add(dateFormat.format(((BoxFile.Info) info).getCreatedAt()));
                   try {
                       list.add(String.valueOf(md.getDouble("/approvalStage")));
                  } catch (Exception e) {
                       //The getter methods on the metadata object returns a null pointer exception if it has no value!!!
                       list.add("");
                  }
                   list.add(((BoxFile.Info) info).getUploaderDisplayName());
                   //Add the link to the application
                   list.add("=HYPERLINK(\"https://cloud.app.box.com/file/" + info.getID() + "\")");

                   list.add(md.getString("/organizationName"));
                   list.add(md.getString("/organizationDescription"));
                   list.add(md.getString("/organizationWebsite"));
                   list.add(md.getString("/organizationCategoryChildWelfareOrCrisisResponseOEnvironment"));
                   list.add(md.getString("/whatRegionscountriesWillYouOperateThisProjectOutOf"));
                   list.add(md.getString("/totalBudgetFromLastYear"));
                   try {
                       list.add(String.join(",", md.getMultiSelect("/howDidYouHearAboutThisGrantCallForProposal")));
                   } catch (Exception e) {
                       //The getter methods on the metadata object returns a null pointer exception if it has no value!!!
                       list.add("");
                   }
                   csvPrinter.printRecord(list);
                   applicants.add(((BoxFile.Info) info).getUploaderDisplayName());
               }
           }
        }
        csvPrinter.close(true);
    }
}
