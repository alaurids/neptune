import requests
import pandas as pd

# list of pacific species
pacificSpecies = [
    "BUTTER_CLAM",
    "GEODUCK_CLAM",
    "HORSE_CLAM",
    "LITTLENECK_CLAM",
    "MANILA_CLAM",
    "NUTTALLS_COCKLE",
    "PACIFIC_RAZOR_CLAM",
    "SOFTSHELL_CLAM",
    "VARNISH_CLAM",
    "BLUE_MUSSEL",
    "CALIFORNIA_MUSSEL",
    "OLYMPIA_OYSTER",
    "PACIFIC_OYSTER",
    "PINK_SCALLOP",
    "PURPLE_HINGE_ROCK_SCALLOP",
    "SPINY_SCALLOP",
    "WEATHERVANE_SCALLOP"
]

# columns that will be in both categories of layer
universalColumns = [
    "OBJECTID",
    "REASON",
    "ALL_BIVALVES"
]

# columns that will be in type 1 layers (44, 42, 26, 24)
type1Columns = [
    "AUDIENCE",
    "PLACENAMEEN",
    "GEODESCRIPTIONEN",
    "PUBLIC_NOTICE_URL_EN",
    "AREASECTOR",
    "STARTDATETEXTEN",
    "ENDDATETEXTEN"
]

# columns that will be in type 2 layers (2, 21, 20, 22)
type2Columns = [
    "PO_NUM",
    "PLACE_NAME_EN",
    "GEO_DESCRIPTION_EN",
    "PUBLIC_NOTICE_URL",
    "SECTOR",
    "ISSUANCE_DATE_EN",
    "ENFORCE_DATE_EN"
]

type1fields = universalColumns + pacificSpecies + type1Columns
type2fields = universalColumns + pacificSpecies + type2Columns

layers = [
    {
        "name": "layer_44",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/44/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type1fields),
        "returnGeometry": "false"
    },
    {
        "name": "layer_42",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/42/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type1fields),
        "returnGeometry": "false"
    },
    {
        "name": "layer_26",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/26/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type1fields),
        "returnGeometry": "false"
    },
    {
        "name": "layer_24",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/24/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type1fields),
        "returnGeometry": "false"
    },
    {
        "name": "layer_2",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/2/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type2fields),
        "returnGeometry": "false"
    },
    {
        "name": "layer_21",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/21/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type2fields),
        "returnGeometry": "false"
    },
    {
        "name": "layer_20",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/20/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type2fields),
        "returnGeometry": "false"
    },
    {
        "name": "layer_22",
        "url": "https://egisp.dfo-mpo.gc.ca/arcgis/rest/services/CSSP/CSSP_Base_Public/MapServer/22/query",
        "where": "DFO_REGION=4",
        "outFields":",".join(type2fields),
        "returnGeometry": "false"
    }
]

def fetch_all_features(url, where, outFields, returnGeometry):
    all_features = []
    offset = 0
    page_size = 2000

    while True:
        params = {
            "where": where,
            "outFields": outFields,
            "returnGeometry": returnGeometry,
            "f": "json",
            "resultOffset": offset,
            "resultRecordCount": page_size
        }

        data = requests.get(url, params=params).json()

        # Stop if no features returned
        if "features" not in data or len(data["features"]) == 0:
            break

        all_features.extend(data["features"])

        # Stop if this was the last page
        if not data.get("exceededTransferLimit", False):
            break

        offset += page_size

    return all_features

# export CSV

for layer in layers:
    print(f"Fetching {layer['name']}...")

    features = fetch_all_features(
        url=layer["url"],
        where=layer["where"],
        outFields=layer["outFields"],
        returnGeometry=layer["returnGeometry"]
    )

    if not features:
        print(f"No features found for {layer['name']}")
        continue

    records = [f["attributes"] for f in features]

    df = pd.DataFrame(records)
    filename = f"{layer['name']}.csv"
    df.to_csv(filename, index=False)

    print(f"Saved {filename} with {len(df)} records")
