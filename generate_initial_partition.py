from ete3 import Tree
import sys



 
def convert_to_adjacency_list(tree):
    adjacency_list = {}
    parent_list = []

    # Dummy node counter
    node_counter = 0

    # Recursive function to traverse the ETE3 tree
    def traverse(node, parent):
        nonlocal node_counter

        # Assign a dummy name to internal nodes
        if node.is_leaf():
            node_name = node.name
        else:
            node_name = f"Internal_{node_counter}"
            node_counter += 1

        # Add node to adjacency list
        adjacency_list[node_name] = []

        # Add parent-child relationship
        if parent:
            adjacency_list[parent].append(node_name)
            adjacency_list[node_name].append(parent)
            parent_list.append((parent, node_name))

        # Process child nodes
        for child in node.children:
            traverse(child, node_name)

    # Start the traversal from the root
    traverse(tree.get_tree_root(), None)

    return adjacency_list, parent_list



def calculate_subtree_sizes(adjacency_list):
    # Dictionary to store the subtree size of each node
    subtree_sizes = {}

    # Set to track visited nodes
    visited = set()

    def dfs(node):
        visited.add(node)  # Mark the current node as visited

        size = 0  # Initialize size to 1 for the current node itself

        for child in adjacency_list[node]:
            if child not in visited:
                size += dfs(child)
        if(len(adjacency_list[node])==1):
            size=1
        subtree_sizes[node] = size
        return size

    # Start the traversal from the root node
    root = next(iter(adjacency_list))  # Assume the first node is the root
    dfs(root)

    return subtree_sizes

def find_min_difference_edge(adjacency_list, subtree_sizes, parent_list):
    # Variables to store the minimum difference and the corresponding edge
    min_difference = float('inf')
    min_edge = None

    # Calculate the total number of nodes in the tree
    total_nodes = subtree_sizes["Internal_0"]

    # Iterate over each node in the tree
    for parent, child in parent_list:
        # Calculate the size of the subtree rooted at the child node
        subtree_size = subtree_sizes[child]

        # Calculate the difference based on the formula abs(n - 2m)
        difference = abs(total_nodes - 2 * subtree_size)

        # Update the minimum difference and corresponding edge if a smaller difference is found
        if difference < min_difference:
            min_difference = difference
            min_edge = (parent, child)

    return min_edge

def divide_nodes(adjacency_list, selected_edge):
    print(adjacency_list)
    # Delete the selected edge from the adjacency list
    adjacency_list[selected_edge[0]].remove(selected_edge[1])
    adjacency_list[selected_edge[1]].remove(selected_edge[0])

    # Sets to store the nodes on each side of the deleted edge
    side1 = set()
    side2 = set()

    # Perform DFS traversal starting from each side of the deleted edge
    def dfs(node, side, visited):
        side.add(node)
        visited.add(node)
        for neighbor in adjacency_list[node]:
            if neighbor not in visited:
                dfs(neighbor, side, visited)

    visited = set()

    # Start DFS from the child node of the selected edge
    dfs(selected_edge[1], side1, visited)

    # Start DFS from the parent node of the selected edge
    dfs(selected_edge[0], side2, visited)

    return side1, side2









if __name__ == "__main__":

    # Get the filename from command-line argument
    filename = sys.argv[1]
    output_file=sys.argv[2]

    # Read the tree string from the file
    with open(filename, "r") as file:
        tree_string = file.read()
    ete3_tree = Tree(tree_string, format=1)

    adjacency_list, parent_list = convert_to_adjacency_list(ete3_tree)
    print(parent_list)

    # Print the adjacency list
    for node, neighbors in adjacency_list.items():
        print(f"{node}: {neighbors}")

    subtree_sizes = calculate_subtree_sizes(adjacency_list)

    # Print the subtree sizes
    for node, size in subtree_sizes.items():
        print(f"{node}: {size}")

    # Find the edge with the minimum difference
    min_edge = find_min_difference_edge(adjacency_list, subtree_sizes, parent_list)

    print("Minimum Difference Edge:", min_edge)
    print(min_edge[0])
    print(min_edge[1])


    # Divide the node set into two parts based on the selected edge
    side1, side2 = divide_nodes(adjacency_list, min_edge)

    # Filter out the internal nodes from the leaf nodes
    leaf_nodes_side1 = [node for node in side1 if not node.startswith("Internal")]
    leaf_nodes_side2 = [node for node in side2 if not node.startswith("Internal")]


    with open(output_file, "w") as output_file:
        # Print leaf nodes from side1 to the file
        for node in leaf_nodes_side1:
            output_file.write(f"{node}, -1\n")

        # Print leaf nodes from side2 to the file
        for node in leaf_nodes_side2:
            output_file.write(f"{node}, 1\n")




























