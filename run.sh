
input_folder="input_gene_trees"
output_folder="output_trees"
partition_folder="generated_partitions"
quartet_generation_file="wqfm_light"
output_quartets="generate_quartets"
wqfm_gen_tree="wqfm_trees"
# Check if input folder exists
'''if [ ! -d "$input_folder" ]; then
  echo "Input folder '$input_folder' does not exist."
  exit 1
fi

# Iterate through files in the input folder
for file in "$input_folder"/*; do
  if [ -f "$file" ]; then
    filename=$(basename "$file")    # Get the filename with extension
    new_filepath="$output_folder/$filename"
    #echo "Full name: $filename"
    ./raxml-ng --consense MRE --tree $file --prefix $new_filepath
  fi
done

find "$output_folder" -type f -name "*.log" -delete
'''
for filename in "$output_folder"/*; do
  if [ -f "$filename" ]; then
    filename_real=$(basename "$filename")
    #echo "Full name: $filename"
    new_filepath="$partition_folder/$filename_real"
    python generate_initial_partition.py $filename $new_filepath
  fi
done


#generate thvse quartets
# Iterate through files in the input folder
'''for file in "$input_folder"/*; do
  if [ -f "$file" ]; then
    filename=$(basename "$file")    # Get the filename with extension
    #new_filepath="$output_folder/$filename"
    echo "Full name--: $file"
    output_path="$output_quartets/$filename"
    #output_path="${output_path%.*}"
    echo "output--> : $output_path"
    cd wqfm_light
    bash quartet-controller.sh ../$file ../$output_path
    cd ..
    
  fi
 
done'''



for file in "$output_quartets"/*; do
  if [ -f "$file" ]; then
    filename=$(basename "$file")    # Get the filename with extension
    #new_filepath="$output_folder/$filename"
    echo "Full name--: $file"
    output_path="$output_quartets/$filename"
    #output_path="${output_path%.*}"
    cd wqfm_light
    /usr/bin/env /usr/lib/jvm/java-11-openjdk-amd64/bin/java @/tmp/cp_8djtompygkwkn4zv4st8i276w.argfile wqfm.main.Main --input_file=../$file --output_file=../$wqfm_gen_tree/1$filename --state=0
    
    /usr/bin/env /usr/lib/jvm/java-11-openjdk-amd64/bin/java @/tmp/cp_8djtompygkwkn4zv4st8i276w.argfile wqfm.main.Main --input_file=../$file --output_file=../$wqfm_gen_tree/2$filename --state=1
    
    cat ../$wqfm_gen_tree/1$filename ../$wqfm_gen_tree/2$filename >> ../$wqfm_gen_tree/3$filename
    #cat ../$wqfm_gen_tree/1$filename && echo "" >> ../$wqfm_gen_tree/3$filename && cat ../$wqfm_gen_tree/2$filename >> ../$wqfm_gen_tree/3$filename

    cd ..
    
  fi
 
done







