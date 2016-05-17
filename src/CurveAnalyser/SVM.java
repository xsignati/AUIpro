package CurveAnalyser;

import libsvm.*;
import java.io.*;
import java.util.*;

/**
 * Created by Flexscan2243 on 06.05.2016.
 *Chih-Chung Chang and Chih-Jen Lin, LIBSVM: a library for
 *support vector machines, 2001.
 *Software available at http://www.csie.ntu.edu.tw/~cjlin/libsvm
 *
 * AUI Modified version
 */

public class SVM {
    private svm_parameter param;		// set by parse_command_line
    private svm_problem prob;		// set by read_problem
    private svm_model model;
    private String input_file_name;		// set by parse_command_line
    private String model_file_name;		// set by parse_command_line
    private String error_msg;
    private int cross_validation;
    private int nr_fold;

    /**
     * AUI parameters
     */
    private double[] arrayC;
    private double[] arrayGamma;
    private double bestC;
    private double bestGamma;
    private double bestAcc;

    private static svm_print_interface svm_print_null = new svm_print_interface()
    {
        public void print(String s) {}
    };

    private static void exit_with_help()
    {
        System.out.print(
                "Usage: svm_train [options] training_set_file [model_file]\n"
                        +"options:\n"
                        +"-s svm_type : set type of SVM (default 0)\n"
                        +"	0 -- C-SVC		(multi-class classification)\n"
                        +"	1 -- nu-SVC		(multi-class classification)\n"
                        +"	2 -- one-class SVM\n"
                        +"	3 -- epsilon-SVR	(regression)\n"
                        +"	4 -- nu-SVR		(regression)\n"
                        +"-t kernel_type : set type of kernel function (default 2)\n"
                        +"	0 -- linear: u'*v\n"
                        +"	1 -- polynomial: (gamma*u'*v + coef0)^degree\n"
                        +"	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
                        +"	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
                        +"	4 -- precomputed kernel (kernel values in training_set_file)\n"
                        +"-d degree : set degree in kernel function (default 3)\n"
                        +"-g gamma : set gamma in kernel function (default 1/num_features)\n"
                        +"-r coef0 : set coef0 in kernel function (default 0)\n"
                        +"-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
                        +"-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
                        +"-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
                        +"-m cachesize : set cache memory size in MB (default 100)\n"
                        +"-e epsilon : set tolerance of termination criterion (default 0.001)\n"
                        +"-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
                        +"-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
                        +"-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
                        +"-v n : n-fold cross validation mode\n"
                        +"-q : quiet mode (no outputs)\n"
        );
        System.exit(1);
    }

    private void gridSearch(double[]rangeC, double[]rangeGamma){
        /**
         * AUI
         * Prepare grids from arguments: rangeC and rangeGamma
         */
        double[][]arrayAcc = null;
        try{
            int cSize;
            int gammaSize;

            cSize = (int)((rangeC[2] - rangeC[0])/rangeC[1]) + 1;
            gammaSize = (int)((rangeGamma[2] - rangeGamma[0])/rangeGamma[1]) + 1;
            arrayC = new double[cSize];
            arrayGamma = new double[gammaSize];
            arrayC[0] = rangeC[0];
            arrayGamma[0] = rangeGamma[0];
            for (int i = 1 ; i < arrayC.length ; i++){
                arrayC[i] += arrayC[i-1] + rangeC[1];
            }
            for (int i = 1 ; i < arrayGamma.length ; i++){
                arrayGamma[i] += arrayGamma[i-1] + rangeGamma[1];
            }

            /**
             * array for accuracies
             */
            if(arrayGamma != null) {
                arrayAcc = new double[cSize][gammaSize];
            }
            else{
                arrayAcc = new double[cSize][0];
            }
        }
        catch(Exception e){System.out.println("Bad allocation in arrays");}

        if (param.kernel_type == svm_parameter.LINEAR && arrayGamma != null) {
            this.arrayGamma = null;
        }

        if (this.arrayGamma != null) {
            // search for C and gamma
            for (int i = 0; i < arrayC.length; i++) {
                for (int j = 0; j < arrayGamma.length; j++) {
                    param.C = Math.pow(2,arrayC[i]);
                    param.gamma = Math.pow(2,arrayGamma[j]);
                    arrayAcc[i][j] = do_cross_validation();
                }// gamma
            }// C
        } else {
            // search for C
            for (int i = 0; i < arrayC.length; i++) {
                arrayAcc[i][0] = do_cross_validation();
            }// C
        }

        /**
         * find best parameters
         */
        int cMaxIndex = 0;
        int gammaMaxIndex = 0;
        for (int i = 0; i < arrayAcc.length; i++) {
            for (int j = 0 ; j < arrayAcc[0].length; j++){
                if (arrayAcc[i][j] > arrayAcc[cMaxIndex][gammaMaxIndex]) {
                    cMaxIndex = i;
                    gammaMaxIndex = j;
                }
            }
        }
        bestC = Math.pow(2,arrayC[cMaxIndex]);

        if (this.arrayGamma != null) {
            bestGamma = Math.pow(2,arrayGamma[gammaMaxIndex]);
            bestAcc = 100.0*arrayAcc[cMaxIndex][gammaMaxIndex]/prob.l;
        }
        else{
            bestAcc =  100.0*arrayAcc[cMaxIndex][0]/prob.l;
        }
    }

    private int do_cross_validation()
    {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];

        svm.svm_cross_validation(prob,param,nr_fold,target);
        if(param.svm_type == svm_parameter.EPSILON_SVR ||
                param.svm_type == svm_parameter.NU_SVR)
        {
            for(i=0;i<prob.l;i++)
            {
                double y = prob.y[i];
                double v = target[i];
                total_error += (v-y)*(v-y);
                sumv += v;
                sumy += y;
                sumvv += v*v;
                sumyy += y*y;
                sumvy += v*y;
            }
            System.out.print("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
            System.out.print("Cross Validation Squared correlation coefficient = "+
                    ((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
                            ((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n"
            );
            return 0;
        }
        else
        {
            for(i=0;i<prob.l;i++)
                if(target[i] == prob.y[i])
                    ++total_correct;
            System.out.print("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
            System.out.print("C parameter: " + param.C + " Gamma parameter: " + param.gamma);
            return total_correct;

        }
    }

    public void run(String argv[]) throws IOException
    {
        parse_command_line(argv);
        read_problem();
        error_msg = svm.svm_check_parameter(prob,param);

        if(error_msg != null)
        {
            System.err.print("ERROR: "+error_msg+"\n");
            System.exit(1);
        }

        if(cross_validation != 0)
        {
            double[] cRange = {-5,2,15};
            double[] gammaRange = {-15,2,3};

            gridSearch(cRange,gammaRange);
            param.C = bestC;
            param.gamma = bestGamma;
            System.out.println("ENDED, BEST C: " + bestC + " BEST GAMMA: " + bestGamma + "BEST ACCURANCY: " + bestAcc );
        }

        model = svm.svm_train(prob,param);
        svm.svm_save_model(model_file_name,model);
        System.out.println("model saved");

    }

    private static double atof(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }

    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    private void parse_command_line(String argv[])
    {
        int i;
        svm_print_interface print_func = null;	// default printing to stdout

        param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0;	// 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        cross_validation = 0;

        // parse options
        for(i=0;i<argv.length;i++)
        {
            if(argv[i].charAt(0) != '-') break;
            if(++i>=argv.length)
                exit_with_help();
            switch(argv[i-1].charAt(1))
            {
                case 's':
                    param.svm_type = atoi(argv[i]);
                    break;
                case 't':
                    param.kernel_type = atoi(argv[i]);
                    break;
                case 'd':
                    param.degree = atoi(argv[i]);
                    break;
                case 'g':
                    param.gamma = atof(argv[i]);
                    break;
                case 'r':
                    param.coef0 = atof(argv[i]);
                    break;
                case 'n':
                    param.nu = atof(argv[i]);
                    break;
                case 'm':
                    param.cache_size = atof(argv[i]);
                    break;
                case 'c':
                    param.C = atof(argv[i]);
                    break;
                case 'e':
                    param.eps = atof(argv[i]);
                    break;
                case 'p':
                    param.p = atof(argv[i]);
                    break;
                case 'h':
                    param.shrinking = atoi(argv[i]);
                    break;
                case 'b':
                    param.probability = atoi(argv[i]);
                    break;
                case 'q':
                    print_func = svm_print_null;
                    i--;
                    break;
                case 'v':
                    cross_validation = 1;
                    nr_fold = atoi(argv[i]);
                    if(nr_fold < 2)
                    {
                        System.err.print("n-fold cross validation: n must >= 2\n");
                        exit_with_help();
                    }
                    break;
                case 'w':
                    ++param.nr_weight;
                {
                    int[] old = param.weight_label;
                    param.weight_label = new int[param.nr_weight];
                    System.arraycopy(old,0,param.weight_label,0,param.nr_weight-1);
                }

                {
                    double[] old = param.weight;
                    param.weight = new double[param.nr_weight];
                    System.arraycopy(old,0,param.weight,0,param.nr_weight-1);
                }

                param.weight_label[param.nr_weight-1] = atoi(argv[i-1].substring(2));
                param.weight[param.nr_weight-1] = atof(argv[i]);
                break;
                default:
                    System.err.print("Unknown option: " + argv[i-1] + "\n");
                    exit_with_help();
            }
        }

        svm.svm_set_print_string_function(print_func);

        // determine filenames

        if(i>=argv.length)
            exit_with_help();

        input_file_name = argv[i];

        if(i<argv.length-1)
            model_file_name = argv[i+1];
        else
        {
            int p = argv[i].lastIndexOf('/');
            ++p;	// whew...
            model_file_name = argv[i].substring(p)+".model";
        }
    }

    // read in a problem (in svmlight format)

    private void read_problem() throws IOException
    {
        BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        while(true)
        {
            String line = fp.readLine();
            if(line == null) break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            vy.addElement(atof(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = atoi(st.nextToken());
                x[j].value = atof(st.nextToken());
            }
            if(m>0) max_index = Math.max(max_index, x[m-1].index);
            vx.addElement(x);
        }

        prob = new svm_problem();
        prob.l = vy.size();
        prob.x = new svm_node[prob.l][];
        for(int i=0;i<prob.l;i++)
            prob.x[i] = vx.elementAt(i);
        prob.y = new double[prob.l];
        for(int i=0;i<prob.l;i++)
            prob.y[i] = vy.elementAt(i);

        if(param.gamma == 0 && max_index > 0)
            param.gamma = 1.0/max_index;

        if(param.kernel_type == svm_parameter.PRECOMPUTED)
            for(int i=0;i<prob.l;i++)
            {
                if (prob.x[i][0].index != 0)
                {
                    System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                    System.exit(1);
                }
                if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
                {
                    System.err.print("Wrong input format: sample_serial_number out of range\n");
                    System.exit(1);
                }
            }

        fp.close();
    }
    //////////

    private static svm_print_interface svm_print_null2 = new svm_print_interface()
    {
        public void print(String s) {}
    };

    private static svm_print_interface svm_print_stdout2 = new svm_print_interface()
    {
        public void print(String s)
        {
            System.out.print(s);
        }
    };

    private static svm_print_interface svm_print_string2 = svm_print_stdout2;

    static void info2(String s)
    {
        svm_print_string2.print(s);
    }

    private static double atof2(String s)
    {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi2(String s)
    {
        return Integer.parseInt(s);
    }

    private static void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException
    {
        int correct = 0;
        int total = 0;
        double error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

        int svm_type=svm.svm_get_svm_type(model);
        int nr_class=svm.svm_get_nr_class(model);
        double[] prob_estimates=null;

        if(predict_probability == 1)
        {
            if(svm_type == svm_parameter.EPSILON_SVR ||
                    svm_type == svm_parameter.NU_SVR)
            {
                info2("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
            }
            else
            {
                int[] labels=new int[nr_class];
                svm.svm_get_labels(model,labels);
                prob_estimates = new double[nr_class];
                output.writeBytes("labels");
                for(int j=0;j<nr_class;j++)
                    output.writeBytes(" "+labels[j]);
                output.writeBytes("\n");
            }
        }
        while(true)
        {
            String line = input.readLine();
            if(line == null) break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            double target = atof2(st.nextToken());
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = atoi2(st.nextToken());
                x[j].value = atof2(st.nextToken());
            }

            double v;
            if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
            {
                v = svm.svm_predict_probability(model,x,prob_estimates);
                output.writeBytes(v+" ");
                for(int j=0;j<nr_class;j++)
                    output.writeBytes(prob_estimates[j]+" ");
                output.writeBytes("\n");
            }
            else
            {
                v = svm.svm_predict(model,x);
                output.writeBytes(v+"\n");
            }

            if(v == target)
                ++correct;
            error += (v-target)*(v-target);
            sumv += v;
            sumy += target;
            sumvv += v*v;
            sumyy += target*target;
            sumvy += v*target;
            ++total;
        }
        if(svm_type == svm_parameter.EPSILON_SVR ||
                svm_type == svm_parameter.NU_SVR)
        {
            info2("Mean squared error = "+error/total+" (regression)\n");
            info2("Squared correlation coefficient = "+
                    ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
                            ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
                    " (regression)\n");
        }
        else
            info2("Accuracy = "+(double)correct/total*100+
                    "% ("+correct+"/"+total+") (classification)\n");
    }

    private static void exit_with_help2()
    {
        System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
                +"options:\n"
                +"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
                +"-q : quiet mode (no outputs)\n");
        System.exit(1);
    }

    public static void runPredict(String argv[]) throws IOException
    {
        int i, predict_probability=0;
        svm_print_string2 = svm_print_stdout2;

        // parse options
        for(i=0;i<argv.length;i++)
        {
            if(argv[i].charAt(0) != '-') break;
            ++i;
            switch(argv[i-1].charAt(1))
            {
                case 'b':
                    predict_probability = atoi2(argv[i]);
                    break;
                case 'q':
                    svm_print_string2 = svm_print_null;
                    i--;
                    break;
                default:
                    System.err.print("Unknown option: " + argv[i-1] + "\n");
                    exit_with_help2();
            }
        }
        if(i>=argv.length-2)
            exit_with_help2();
        try
        {
            BufferedReader input = new BufferedReader(new FileReader(argv[i]));
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
            svm_model model = svm.svm_load_model(argv[i+1]);
            if (model == null)
            {
                System.err.print("can't open model file "+argv[i+1]+"\n");
                System.exit(1);
            }
            if(predict_probability == 1)
            {
                if(svm.svm_check_probability_model(model)==0)
                {
                    System.err.print("Model does not support probabiliy estimates\n");
                    System.exit(1);
                }
            }
            else
            {
                if(svm.svm_check_probability_model(model)!=0)
                {
                    info2("Model supports probability estimates, but disabled in prediction.\n");
                }
            }
            predict(input,output,model,predict_probability);
            input.close();
            output.close();
        }
        catch(FileNotFoundException e)
        {
            exit_with_help2();
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            exit_with_help2();
        }
    }
}
