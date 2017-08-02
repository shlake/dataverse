package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.util.logging.Logger;
import static javax.ws.rs.core.Response.Status.OK;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import org.junit.After;
import org.junit.Assert;

/**
 *
 * @author bsilverstein
 */
public class S3AccessIT {
    
    private static final Logger logger = Logger.getLogger(S3AccessIT.class.getCanonicalName());

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
        
    }
    
    
    //tests:
    //testStorageIdentifier
    //testAddDataset
    //testPublishDataset
        //testAddFilesToPublishedDataset    
    @Test
    public void testAddDataFileS3Prefix() {
        //create user who will make a dataverse/dataset
        Response createUser = UtilIT.createRandomUser();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);
        
        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);
        
        Response createDatasetResponse = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        Integer datasetId = JsonPath.from(createDatasetResponse.body().asString()).getInt("data.id");
        createDatasetResponse.prettyPrint();
        
        //upload a tabular file via native
        String pathToFile = "scripts/search/data/tabular/1char";
        Response addFileResponse = UtilIT.uploadFileViaNative(datasetId.toString(), pathToFile, apiToken);
        addFileResponse.prettyPrint();
        addFileResponse.then().assertThat()
                .body("data.files[0].dataFile.storageIdentifier", startsWith("s3://"));
        
        Response deleteDataset = UtilIT.deleteDatasetViaNativeApi(datasetId, apiToken);
        deleteDataset.prettyPrint();
        deleteDataset.then().assertThat()
                .statusCode(200);

        Response deleteDataverse = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverse.prettyPrint();
        deleteDataverse.then().assertThat()
                .statusCode(200);
        
        Response deleteUser = UtilIT.deleteUser(username);
        deleteUser.prettyPrint();
        deleteUser.then().assertThat()
                .statusCode(200);

        
    }
    
    @After
    public void tearDownClass() {
        
    }
   
}
