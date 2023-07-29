package wqfm.algo;

import wqfm.configs.Config;
import wqfm.bip.InitialBipartition;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import wqfm.bip.Bipartition_8_values;
import wqfm.ds.CustomDSPerLevel;
import wqfm.ds.FMResultObject;
import wqfm.ds.InitialTable;
import wqfm.ds.Quartet;
import wqfm.utils.Helper;
import wqfm.utils.TreeHandler;
import wqfm.bip.WeightedPartitionScores;
import wqfm.configs.DefaultValues;

/**
 *
 * @author mahim
 */
public class FMRunner {

    //Main method to run all functions ... [ABSTRACT everything from Main class]
    public static String runFunctions() {
        
        FMRunner runner = new FMRunner();
        InitialTable initialTable = new InitialTable();
        CustomDSPerLevel customDS = new CustomDSPerLevel();
        runner.readFileAndPopulateInitialTables(Config.INPUT_FILE_NAME, customDS, initialTable);
        System.out.println("Reading from file ...<" + Config.INPUT_FILE_NAME + "> done."
                + "\nInitial-Num-Quartets = " + initialTable.sizeTable());
        System.out.println("Running with partition score " + WeightedPartitionScores.GET_PARTITION_SCORE_PRINT());
        int level = 0;
        customDS.level = level; //for debugging issues.

        System.out.println(InitialTable.TAXA_COUNTER);

        System.out.println(InitialTable.map_of_str_vs_int_tax_list);
        System.out.println(InitialTable.map_of_int_vs_str_tax_list);




        // New part added


        System.out.println("---------------------------");
        System.out.println("STATE IS: "+Config.STATE);
        //String fileName = "../generated_partitions/file_1.txt.raxml.consensusTreeMRE";
        Map<Integer,Integer>partitions=new HashMap<>();
        String filename_input=Config.INPUT_FILE_NAME;
        String partition_file="../generated_partitions/"+filename_input.split("/")[2]+".raxml.consensusTreeMRE";
        try (BufferedReader br = new BufferedReader(new FileReader(partition_file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String name=line.split(",")[0] ;
                int value=Integer.parseInt(line.split(", ")[1]);
                partitions.put(InitialTable.map_of_str_vs_int_tax_list.get(name),value);
                //System.out.println(line);

            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        System.out.println(partitions);
        System.out.println("---------------------------");










        String final_tree = runner.recursiveDivideAndConquer(customDS, level, initialTable,partitions); //customDS will have (P, Q, Q_relevant etc) all the params needed.
        System.out.println("\n\n[L 49.] FMRunner: final tree return");

//        System.out.println(final_tree);
        String final_tree_decoded = Helper.getFinalTreeFromMap(final_tree, InitialTable.map_of_int_vs_str_tax_list);
        System.out.println(final_tree_decoded);
        Helper.writeToFile(final_tree_decoded, Config.OUTPUT_FILE_NAME);
        
        return final_tree_decoded;
    }

    // ------>>>> Main RECURSIVE function ....
    private String recursiveDivideAndConquer(CustomDSPerLevel customDS_this_level, int level, InitialTable initialTable,Map<Integer,Integer>partitions) {
        /*So that when customDS is passed subsequently, automatic sorting will be done. No need to do it somewhere else*/
        if (level == 0) { //only do this during level 0 [at the START]
            customDS_this_level.setInitialTableReference(initialTable); //change reference of initial table.
        }
        customDS_this_level.sortQuartetIndicesMap(); //sort the quartet-index map for initial-bipartition-computation [NOT set of quartets]
        customDS_this_level.fillRelevantQuartetsMap(); //fill-up the relevant quartets per taxa map
        if (level == 0) { //only do it for the initial step, other levels will be passed as parameters
            customDS_this_level.fillUpTaxaList(); //fill-up the taxa list
            System.out.println("Total Num-Taxa = " + customDS_this_level.taxa_list_int.size());
        }

        /////////////////// TERMINATING CONDITIONS \\\\\\\\\\\\\\\\\\\\\\\\
        // |P| <= 3 OR |Q|.isEmpty() ... return star over taxa list{P}
        if ((customDS_this_level.taxa_list_int.size() <= 3)
                || (customDS_this_level.quartet_indices_list_unsorted.isEmpty())) {
            String starTree = TreeHandler.getStarTree(customDS_this_level.taxa_list_int); //depth-one tree
            return starTree;
        }

        level++; // For dummy node finding.
        customDS_this_level.level = level; //for debugging issues.

        InitialBipartition initialBip = new InitialBipartition();
        Map<Integer, Integer> mapInitialBipartition = initialBip.getInitialBipartitionMap(customDS_this_level);

        if(level==1 && Config.STATE=="1"){
            System.out.println("Partition: "+ mapInitialBipartition);
            System.out.println(mapInitialBipartition.size());
            for(int i=0;i<mapInitialBipartition.size();i++){
                mapInitialBipartition.put(i,partitions.get(i));
            }
            System.out.println("partition 2: "+mapInitialBipartition);
        }
        if (Config.DEBUG_MODE_PRINTING_GAINS_BIPARTITIONS) {
            System.out.println("L 84. FMComputer. Printing initialBipartition.");
            Helper.printPartition(mapInitialBipartition, DefaultValues.LEFT_PARTITION, DefaultValues.RIGHT_PARTITION, InitialTable.map_of_int_vs_str_tax_list);
        }

        Bipartition_8_values initialBip_8_vals = new Bipartition_8_values();
        initialBip_8_vals.compute8ValuesUsingAllQuartets_this_level(customDS_this_level, mapInitialBipartition);
        System.out.println(WeightedPartitionScores.GET_PARTITION_SCORE_PRINT() + " LEVEL: " + level + ", ALPHA: " + WeightedPartitionScores.ALPHA_PARTITION_SCORE + ", BETA: " + WeightedPartitionScores.BETA_PARTITION_SCORE);

        FMComputer fmComputerObject = new FMComputer(customDS_this_level, mapInitialBipartition, initialBip_8_vals, level);
        FMResultObject fmResultObject = fmComputerObject.run_FM_Algorithm_Whole();

        CustomDSPerLevel customDS_left = fmResultObject.customDS_left_partition;
        CustomDSPerLevel customDS_right = fmResultObject.customDS_right_partition;

        //Debug printing begin
        //        System.out.println("-------------- After Level " + level + " LEFT Quartets -------------------- ");
        //        System.out.println(customDS_left.onlyQuartetIndices());
        //        System.out.println(customDS_left.taxa_list_int);
        //        System.out.println("============== After Level " + level + " RIGHT Quartets ==================== ");
        //        System.out.println(customDS_right.onlyQuartetIndices());
        //        System.out.println(customDS_right.taxa_list_int);
        //Debug printing end
        /////////////////// Beginning of Recursion \\\\\\\\\\\\\\\\\\\\\\\\\\\
        int dummyTaxon = fmResultObject.dummyTaxonThisLevel;
        String left_tree_unrooted = recursiveDivideAndConquer(customDS_left, level, initialTable,partitions);
        String right_tree_unrooted = recursiveDivideAndConquer(customDS_right, level, initialTable,partitions);
        String merged_tree = TreeHandler.mergeUnrootedTrees(left_tree_unrooted, right_tree_unrooted, String.valueOf(dummyTaxon));
        return merged_tree;
    }

    // https://stackoverflow.com/questions/6100712/simple-way-to-count-character-occurrences-in-a-string/23906674
    private int countChars_in_String(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    //------------------Initial Bipartition --------------------
    private void populatePerInputLine(CustomDSPerLevel customDS, InitialTable initialTable, String line, int line_cnt) {
        // Check if STAR is present 
        // ((1,2,49,57)); 6     is a STAR
        // ((0,1),(10,9)); 343  is normal // should have 3 right brackets and 3 left brackets

        int cnt_left_brackets = countChars_in_String(line, '(');
        int cnt_right_brackets = countChars_in_String(line, ')');

        if ((cnt_left_brackets != cnt_right_brackets) || (cnt_left_brackets != 3) || (cnt_right_brackets != 3)) {
            System.out.println("\n\n****** Found STAR, line num " + line_cnt + " :->" + line);
            System.out.println("\nCan't handle polytomy for now. Exiting System.\n\n");
            System.exit(-1);
        }

        // No issues with STAR.
        Quartet quartet = new Quartet(line);
        initialTable.addToListOfQuartets(quartet); //add to initial-quartets-single-list
        int idx_qrt_in_table_1 = initialTable.sizeTable() - 1; //size - 1 is the last index
        customDS.quartet_indices_list_unsorted.add(idx_qrt_in_table_1);
    }

    //BufferedReader is used here since BufferedReader is faster than Scanner.readLine
    public void readFileAndPopulateInitialTables(String inputFileName, CustomDSPerLevel customDS, InitialTable initialTable) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(inputFileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                int line_cnt = 1;
                while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                    populatePerInputLine(customDS, initialTable, line, line_cnt);
                    line_cnt++;
                }
            }
        } catch (IOException ex) {
            System.out.println("ERROR READING FILE <" + inputFileName + ">. EXITING SYSTEM");
            System.exit(-1);

        } finally {
            try {
                fileInputStream.close();
            } catch (IOException ex) {
                System.out.println("ERROR IN CLOSING fileInputStream while reading file. Exiting");
                System.exit(-1);
            }
        }
        //FeatureComputer.Compute_Feature(initialTable.get_QuartetList());

    }

    //-------------------------------------------------------------------------------------------------------------
}
