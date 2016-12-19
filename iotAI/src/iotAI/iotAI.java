package iotAI;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

public class iotAI {
	public static final String URL = "jdbc:mysql://127.0.0.1:3306/lightdb";
	public final static String USER = "roy";
	public final static String PASSWORD = "m17j05";
//	jdbcDriver=org.gjt.mm.mysql.Driver;
//	jdbcURL=jdbc:mysql://localhost:3306/some_database
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			
		//1. reading arff 當作模型training
		 BufferedReader reader = new BufferedReader(
                 new FileReader("data/training.arff"));
		 Instances training = new Instances(reader);
		 reader.close();
		 // setting class attribute
		 //data.setClassIndex(data.numAttributes() - 1);
		 System.out.print(training);
		 
		//2. user arff to building weka model
		// 設定要分類的屬性
			training.setClassIndex(training.numAttributes() - 1);

			// 實作貝氏分類
			NaiveBayes nb = new NaiveBayes();

			// 開始訓練
			nb.buildClassifier(training);


			/*
			 * 分類 
			 * 基於測試:這邊使用訓練集的資料來做分類
			 */
//			double result = nb.classifyInstance(training.instance(0));
			System.out.println("original value is:"+ training.instance(0).value(0));
			System.out.println("original label is:" + training.instance(0).value(1));
//			System.out.println("nb label result is:" + result);
			
		//3. connect to database to fetch 
			
			InstanceQuery query = new InstanceQuery();
			query.setUsername(USER);
			query.setPassword(PASSWORD);
			query.setDatabaseURL(URL);
			query.setQuery("select * from light");
			Instances data = query.retrieveInstances();

			//顯示資料
			System.out.println(data);
			
		//4. perform pre-proceessing on testing data as an input for model
			Remove remove=new Remove();

			String[] options= new String[2];
			options[0]="-R"; //範圍
			options[1]="1,4"; //第1個、第4個屬性
			        
			remove.setOptions(options);
			remove.setInputFormat(data);
			Instances dataClusterer = Filter.useFilter(data, remove);
			
			NumericToNominal convert= new NumericToNominal();

			options[0]="-R";
			options[1]="2";

			convert.setOptions(options);
			convert.setInputFormat(dataClusterer);
			Instances Dataset=Filter.useFilter(dataClusterer, convert);
			System.out.println(Dataset);
			
		//5. use model to get classification result
//			 * 分類 
//			 * 基於測試:這邊使用訓練集的資料來做分類
//			 */
			Dataset.setClassIndex(Dataset.numAttributes() - 1);
			double result = nb.classifyInstance(Dataset.instance(0));
			System.out.println("Colum1 value is:" + Dataset.instance(0).value(0));
			System.out.println("Colum2 value is:" + Dataset.instance(0).value(1));
			System.out.println("nb result label is:" + result);
		//6. compare result with the status on the database, if not the same update
			String sql;
			for(int i = 0; i < Dataset.numInstances(); i ++)
			{
				result = nb.classifyInstance(Dataset.instance(i));
				if (result != data.instance(i).value(3))
				{
//					System.out.println(data.instance(i).value(0));
					sql = "UPDATE "+ "light" +" SET status ="+String.valueOf(result)+" WHERE id="+data.instance(i).value(0);
					query.execute(sql);
				}
			}
	}

}
