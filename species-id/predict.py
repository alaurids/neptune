# Predicts the species name of one image

import torch
import torch.nn as nn
from torchvision import transforms, models
from PIL import Image
import torch.nn.functional as F

# transform image to match train.py
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

# load model
num_classes = 18
class_names = ['blue-mussel', 'butter-clam', 'california-mussel', 'geoduck', 'littleneck-clam',
               'manila-clam', 'northern-abalone', 'nuttalls-cockle', 'olympia-oyster', 'pacific-gaper',
               'pacific-oyster', 'pink-scallop', 'purple-scallop', 'razor-clam', 'softshell-clam',
               'spiny-scallop', 'varnish-clam', 'weathervane-scallop']

model = models.mobilenet_v3_large(weights=None)
model.classifier[3] = nn.Linear(model.classifier[3].in_features, num_classes)

model.load_state_dict(torch.load("mobilenetv3_large_model.pth"))
model.eval()

# load image
img_path = "medium (96).jpg"
image = Image.open(img_path).convert("RGB")
image = transform(image).unsqueeze(0)

# predict
with torch.no_grad():
    outputs = model(image)
    probabilities = F.softmax(outputs, dim=1)

# Top 2 predictions
top2_prob, top2_idx = torch.topk(probabilities, k=2)

top1_idx = top2_idx[0][0].item()
top1_conf = top2_prob[0][0].item() * 100

top2_idx_2 = top2_idx[0][1].item()
top2_conf = top2_prob[0][1].item() * 100

print(f"Top prediction: {class_names[top1_idx]} ({top1_conf:.2f}% confidence)")
print(f"Second best:   {class_names[top2_idx_2]} ({top2_conf:.2f}% confidence)")

