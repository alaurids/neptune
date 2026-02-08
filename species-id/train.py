# Train neural network based on pre-downloaded training images sorted into folders
# Currently training MobileNetV3 small and large models to see which is optimal for project use

import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from torchvision import datasets, transforms, models

print("starting training...")

# Image preprocessing
# MobileNetV2: 224x224, normalization
# data augmentation (i.e. random flips, colour augmentation) for more robust detection
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.RandomHorizontalFlip(p=0.5),
    transforms.RandomRotation(10),
    transforms.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.2),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

# Load dataset
train_data = datasets.ImageFolder(
    root="photos",
    transform=transform
)

print("dataset loaded")

# Create DataLoader to feed images in batches
train_loader = DataLoader(
    train_data,
    batch_size=32,
    shuffle=True
)

num_classes = len(train_data.classes)
print("Classes:", train_data.classes)

# Initialize model

# FROM SCRATCH
# model = SimpleCNN()

# MOBILENET V2
# model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.DEFAULT)
# model.classifier[1] = nn.Linear(model.classifier[1].in_features, num_classes)

# MOBILENET V3 LARGE
# model = models.mobilenet_v3_large(weights=models.MobileNet_V3_Large_Weights.DEFAULT)

# MOBILENET V3 SMALL
model = models.mobilenet_v3_small(weights=models.MobileNet_V3_Small_Weights.DEFAULT)
num_classes = len(train_data.classes)

model.classifier[3] = nn.Linear(model.classifier[3].in_features, num_classes)
print ("loaded mobilenet v3 small model")
# Use previously trained model.pth file (DELETE THIS IF STARTING FRESH!!!)
# model.load_state_dict(torch.load("model.pth"))

# Define optimizer and loss function
optimizer = torch.optim.Adam(model.parameters(), lr=0.0005)
criterion = torch.nn.CrossEntropyLoss()

# Training loop
epochs = 10 # number of passes through the dataset

for epoch in range(epochs): 
    running_loss = 0.0

    for images, labels in train_loader:
        optimizer.zero_grad()        # reset gradients
        outputs = model(images)      # forward pass
        loss = criterion(outputs, labels)  # compute loss
        loss.backward()              # compute gradients
        optimizer.step()             # update weights

        running_loss += loss.item()

    avg_loss = running_loss / len(train_loader)
    print(f"Epoch {epoch+1}/{epochs} - Loss: {avg_loss:.4f}")

# Save model .pth file
torch.save(model.state_dict(), "mobilenetv3_small_model.pth")
print("Model saved as mobilenetv3_small_model.pth")


