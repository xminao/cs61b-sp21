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
            case "rm-branch":
                validateNumArgs(args, 2);
                Repository.rm_branch(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                Repository.status();
                break;
            case "log":
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "checkout":
                Repository.checkout(args);
                break;
            case "reset":
                validateNumArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);

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
            case "generateG":
                Repository.testGraph();
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

}
