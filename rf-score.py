from ete3 import Tree
import sys
def calculate_rf_score(tree1, tree2):
    t1 = Tree(tree1)
    t2 = Tree(tree2)

    return t1.robinson_foulds(t2)[0]
    

# Example usage
with open(sys.argv[1], 'r') as file:
     tree1 = file.read()
     
with open(sys.argv[2], 'r') as file:
     tree2 = file.read()
rf_score = calculate_rf_score(tree1, tree2)
print("RF score:", rf_score)

with open(sys.argv[3], "w") as output_file:
        output_file.write("RF score: " + str(rf_score) + "\n")
