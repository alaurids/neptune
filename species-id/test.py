import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import transforms, models
from PIL import Image
import os
import time
import csv

# image preprocessing
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

# config
test_folder = "photos/test_images"
class_names = ['blue-mussel', 'butter-clam', 'california-mussel', 'geoduck', 'littleneck-clam',
               'manila-clam', 'northern-abalone', 'nuttalls-cockle', 'olympia-oyster', 'pacific-gaper',
               'pacific-oyster', 'pink-scallop', 'purple-scallop', 'razor-clam', 'softshell-clam',
               'spiny-scallop', 'varnish-clam', 'weathervane-scallop']
num_classes = len(class_names)

output_csv = "results.csv"

# load models
def load_model_large():
    model = models.mobilenet_v3_large(weights=None)
    model.classifier[3] = nn.Linear(model.classifier[3].in_features, num_classes)
    model.load_state_dict(torch.load("mobilenetv3_large_model.pth"))
    model.eval()
    return model

def load_model_small():
    model = models.mobilenet_v3_small(weights=None)
    model.classifier[3] = nn.Linear(model.classifier[3].in_features, num_classes)
    model.load_state_dict(torch.load("mobilenetv3_small_model.pth"))
    model.eval()
    return model

model_large = load_model_large()
model_small = load_model_small()

# inference
def run_inference(model, image_path):
    image = Image.open(image_path).convert("RGB")
    image = transform(image).unsqueeze(0)

    start = time.time()
    with torch.no_grad():
        outputs = model(image)
        probs = F.softmax(outputs, dim=1)
    end = time.time()

    inference_time = end - start

    top2_prob, top2_idx = torch.topk(probs, k=2)

    top1_idx = top2_idx[0][0].item()
    top1_conf = top2_prob[0][0].item() * 100

    top2_idx_2 = top2_idx[0][1].item()
    top2_conf = top2_prob[0][1].item() * 100

    return {
        "top1_class": class_names[top1_idx],
        "top1_conf": top1_conf,
        "top2_class": class_names[top2_idx_2],
        "top2_conf": top2_conf,
        "time": inference_time
    }

# walk through test folders
rows = []

for species in os.listdir(test_folder):
    print (species)
    species_folder = os.path.join(test_folder, species)
    if not os.path.isdir(species_folder):
        continue

    for img in os.listdir(species_folder):
        if not img.lower().endswith((".jpg", ".jpeg", ".png")):
            continue

        img_path = os.path.join(species_folder, img)
        true_label = species  # folder name is the ground truth

        # Large model
        res_large = run_inference(model_large, img_path)
        correct_large = (res_large["top1_class"] == true_label)

        rows.append([
            img,
            true_label,
            "MobileNetV3-Large",
            res_large["top1_class"],
            f"{res_large['top1_conf']:.2f}",
            res_large["top2_class"],
            f"{res_large['top2_conf']:.2f}",
            f"{res_large['time']*1000:.2f}",
            correct_large
        ])

        # Small model
        res_small = run_inference(model_small, img_path)
        correct_small = (res_small["top1_class"] == true_label)

        rows.append([
            img,
            true_label,
            "MobileNetV3-Small",
            res_small["top1_class"],
            f"{res_small['top1_conf']:.2f}",
            res_small["top2_class"],
            f"{res_small['top2_conf']:.2f}",
            f"{res_small['time']*1000:.2f}",
            correct_small
        ])

# write CSV
with open(output_csv, mode="w", newline="") as file:
    writer = csv.writer(file)
    writer.writerow([
        "image",
        "true_label",
        "model",
        "top1_class",
        "top1_confidence",
        "top2_class",
        "top2_confidence",
        "inference_time_ms",
        "correct"
    ])
    writer.writerows(rows)

print(f"Results saved to {output_csv}")

