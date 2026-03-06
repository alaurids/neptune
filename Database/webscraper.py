import requests
from bs4 import BeautifulSoup
import json
import time

# Matching names
species_to_scrape = [
    "Manila Clam", "Pacific Oyster", "Geoduck", "Blue Mussel", 
    "Butter Clam", "Littleneck Clam", "Northern Abalone" 
]

def get_dfo_data(name):
    
    search_url = f"https://www.pac.dfo-mpo.gc.ca/fm-gp/shellfish-mollusques/index-eng.html"
   
    return {
        "isProtected": "Abalone" in name or "Olympia" in name,
        "habitat": "Found in intertidal gravel and mud" if "Clam" in name else "Rocks and hard substrates"
    }

def get_wiki_data(name):
    url = f"https://en.wikipedia.org/wiki/{name.replace(' ', '_')}"
    res = requests.get(url)
    soup = BeautifulSoup(res.text, 'html.parser')
    
    # Extract scientific name (usually in the first <i> tag of the infobox)
    sci_name = soup.find('i').text if soup.find('i') else "Unknown"
    
    # Extract the first real paragraph for the description
    desc = ""
    for p in soup.find_all('p'):
        if len(p.text) > 50:
            desc = p.text.strip()
            break
            
    return {"scientificName": sci_name, "description": desc[:250] + "..."}

all_species_data = []

for idx, name in enumerate(species_to_scrape):
    print(f"Processing {name}...")
    dfo = get_dfo_data(name)
    wiki = get_wiki_data(name)
    
    combined = {
        "speciesId": idx,
        "commonName": name,
        "scientificName": wiki["scientificName"],
        "description": wiki["description"],
        "habitat": dfo["habitat"],
        "isProtected": dfo["isProtected"]
    }
    all_species_data.append(combined)
    time.sleep(1) # Delay to be respectful

with open('shellfish_data.json', 'w') as f:
    json.dump(all_species_data, f, indent=4)
