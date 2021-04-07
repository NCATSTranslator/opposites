package edu.cuanschutz.ccp.metakg;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parser for the SmartAPI MetaKG JSON: https://smart-api.info/api/metakg
 *
 * <pre>
 *{
  "associations": [
 {
      "subject": "Disease",
      "object": "ChemicalSubstance",
      "predicate": "treated_by",
      "api": {
        "name": "OpenPredict API",
        "smartapi": {
          "metadata": "https://openpredict.semanticscience.org/openapi.json",
          "id": "e9f69b81e755e163fdf6c41a2b5e07c0",
          "ui": "https://smart-api.info/ui/e9f69b81e755e163fdf6c41a2b5e07c0"
        },
        "x-translator": {
          "component": "KP",
          "team": [
            "Clinical Data Provider"
          ]
        }
      }
    },
    ]
}
 * </pre>
 *
 *
 */
public class MetaKgParser {

	public static final String META_KG_URL = "https://smart-api.info/api/metakg";

	/**
	 * @param jsonStreamReader
	 * @return a map where the key is "subjectId_objectId" and the value is a
	 *         {@link Set} of {@link Triple} objects that store the metadata for
	 *         which team/api is responsible for producing the triple.
	 */
	public static Map<String, Set<Triple>> parseMetaKgJson(InputStreamReader jsonStreamReader) {
		Map<String, Set<Triple>> map = new HashMap<String, Set<Triple>>();

		Gson gson = new Gson();
		Associations associations = gson.fromJson(jsonStreamReader, Associations.class);
		for (Association association : associations.getAssociations()) {
			if (association != null) {
				String subj = association.getSubject();
				String pred = association.getPredicate();
				String obj = association.getObject();

				String key = subj + "_" + obj;
				String teamName = CollectionsUtil.createDelimitedString(association.getApi().getXTranslator().getTeam(),
						";");
				Triple triple = new Triple(subj, pred, obj);
				triple.addTeamApi(teamName, association.getApi().getName());

				CollectionsUtil.addToOne2ManyUniqueMap(key, triple, map);
			}
		}

		return map;
	}

	@Data
	public static class Triple {
		private final String subject;
		private final String predicate;
		private final String object;
		private Map<String, String> teamToApiMap = new HashMap<String, String>();

		public void addTeamApi(String teamName, String apiName) {
			teamToApiMap.put(teamName, apiName);
		}
	}

	@Data
	@NoArgsConstructor
	public static class Associations {
		private List<Association> associations;
	}

	@Data
	@NoArgsConstructor
	public static class Association {
		private String subject;
		private String object;
		private String predicate;
		private Api api;

	}

	@Data
	@NoArgsConstructor
	public static class Api {
		private String name;
		@SerializedName("smartapi")
		private SmartApi smartApi;
		@SerializedName("x-translator")
		private XTranslator xTranslator;
	}

	@Data
	@NoArgsConstructor
	public static class SmartApi {
		private String metadata;
		private String id;
		private String ui;
	}

	@Data
	@NoArgsConstructor
	public static class XTranslator {
		private String component;
		private List<String> team;

	}
}
