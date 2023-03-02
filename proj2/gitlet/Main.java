package gitlet;

import java.net.ResponseCache;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author xminao
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }


        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateNumArgs(args, 2);
                Repository.add(args);
                break;
            case "date":
                Commit commit = new Commit("xminao", "null", "date test");
                commit.dateTest();
                break;
            case "cat-file":
                Repository.cat_file(args[1]);
                break;
            case "ls-stage":
                Repository.ls_stage();
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
            // TODO: FILL THE REST IN
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

}
