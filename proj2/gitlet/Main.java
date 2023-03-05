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
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "branch":
                validateNumArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                Repository.status();
                break;
            case "cat-file":
                validateNumArgs(args, 2);
                Repository.cat_file(args[1]);
                break;
            case "ls-stage":
                validateNumArgs(args, 1);
                //Repository.ls_stage();
                break;
            case "typeof":
                validateNumArgs(args, 2);
                System.out.println(Repository.typeOf(args[1]));
                break;
            case "generate":
                System.out.println(Index.generateCommitTree());
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
