package edu.cuanschutz.ccp.metakg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.gson.Gson;

import edu.cuanschutz.ccp.metakg.MetaKgParser;
import edu.cuanschutz.ccp.metakg.MetaKgParser.Api;
import edu.cuanschutz.ccp.metakg.MetaKgParser.Association;
import edu.cuanschutz.ccp.metakg.MetaKgParser.Associations;
import edu.cuanschutz.ccp.metakg.MetaKgParser.SmartApi;
import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.cuanschutz.ccp.metakg.MetaKgParser.XTranslator;

public class MetaKgParserTest {

	// @formatter:off
	private static final String SAMPLE_JSON = "{\n" + 
			"  \"associations\": [\n" + 
			" {\n" + 
			"      \"subject\": \"Disease\",\n" + 
			"      \"object\": \"ChemicalSubstance\",\n" + 
			"      \"predicate\": \"treated_by\",\n" + 
			"      \"api\": {\n" + 
			"        \"name\": \"OpenPredict API\",\n" + 
			"        \"smartapi\": {\n" + 
			"          \"metadata\": \"https://openpredict.semanticscience.org/openapi.json\",\n" + 
			"          \"id\": \"e9f69b81e755e163fdf6c41a2b5e07c0\",\n" + 
			"          \"ui\": \"https://smart-api.info/ui/e9f69b81e755e163fdf6c41a2b5e07c0\"\n" + 
			"        },\n" + 
			"        \"x-translator\": {\n" + 
			"          \"component\": \"KP\",\n" + 
			"          \"team\": [\n" + 
			"            \"Clinical Data Provider\"\n" + 
			"          ]\n" + 
			"        }\n" + 
			"      }\n" + 
			"    }\n" + 
			"    ]\n" + 
			"}";
	// @formatter:on

	@Test
	public void testDeserialization() {
		Gson gson = new Gson();
		Associations associations = gson.fromJson(SAMPLE_JSON, Associations.class);

		System.out.println(associations.toString());

		assertEquals("should be 1 association", 1, associations.getAssociations().size());

		Association association = associations.getAssociations().get(0);

		assertEquals("Disease", association.getSubject());
		assertEquals("treated_by", association.getPredicate());
		assertEquals("ChemicalSubstance", association.getObject());

		Api api = association.getApi();
		assertNotNull(api);

		assertEquals("OpenPredict API", api.getName());
		SmartApi smartApi = api.getSmartApi();
		assertNotNull(smartApi);
		assertEquals("e9f69b81e755e163fdf6c41a2b5e07c0", smartApi.getId());
		assertEquals("https://openpredict.semanticscience.org/openapi.json", smartApi.getMetadata());
		assertEquals("https://smart-api.info/ui/e9f69b81e755e163fdf6c41a2b5e07c0", smartApi.getUi());

		XTranslator xTranslator = api.getXTranslator();
		assertNotNull(xTranslator);
		assertEquals("KP", xTranslator.getComponent());
		assertEquals(Arrays.asList("Clinical Data Provider"), xTranslator.getTeam());
	}

	@Test
	public void testParseMetaKgJson() {
		Map<String, Set<Triple>> map = MetaKgParser
				.parseMetaKgJson(new InputStreamReader(new ByteArrayInputStream(SAMPLE_JSON.getBytes())));

		Map<String, Set<Triple>> expectedMap = new HashMap<String, Set<Triple>>();

		String key = "Disease_ChemicalSubstance";
		Triple t = new Triple("Disease", "treated_by", "ChemicalSubstance");
		t.addTeamApi("Clinical Data Provider", "OpenPredict API");
		Set<Triple> expectedTriples = new HashSet<Triple>(Arrays.asList(t));

		expectedMap.put(key, expectedTriples);

		assertEquals(expectedMap, map);

	}

}
