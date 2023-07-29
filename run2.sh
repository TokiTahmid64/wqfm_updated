input_folder="input_gene_trees"

# Check if input folder exists
if [ ! -d "$input_folder" ]; then
  echo "Input folder '$input_folder' does not exist."
  exit 1
fi

# Iterate through files in the input folder
for file in "$input_folder"/*; do
  if [ -f "$file" ]; then
    echo "Full name: $file"
  fi
done
