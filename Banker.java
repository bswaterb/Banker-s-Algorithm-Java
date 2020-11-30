import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaohui on 2020/11/26
 */
public class Banker {
    //初始可调度资源
    static int staticAvailable[] = new int[]{3,3,2};

    public static void main(String[] args) {

        int max1[] = new int[]{7,5,3};
        int allocation1[] = new int[]{0,1,0};
        int need1[] = new int[]{7,4,3};
        Process process1 = new Process("进程1",max1,allocation1,need1);

        int max2[] = new int[]{3,2,2};
        int allocation2[] = new int[]{2,0,0};
        int need2[] = new int[]{1,2,2};
        Process process2 = new Process("进程2",max2,allocation2,need2);

        int max3[] = new int[]{9,0,2};
        int allocation3[] = new int[]{3,0,2};
        int need3[] = new int[]{6,0,0};
        Process process3 = new Process("进程3",max3,allocation3,need3);

        int max4[] = new int[]{2,2,2};
        int allocation4[] = new int[]{2,1,1};
        int need4[] = new int[]{0,1,1};
        Process process4 = new Process("进程4",max4,allocation4,need4);

        int max5[] = new int[]{4,3,3};
        int allocation5[] = new int[]{0,0,2};
        int need5[] = new int[]{4,3,1};
        Process process5 = new Process("进程5",max5,allocation5,need5);



        //将进程添加到进程队列中
        List processList = new ArrayList<Process>();
        processList.add(process1);
        processList.add(process2);
        processList.add(process3);
        processList.add(process4);
        processList.add(process5);


        //判断初始状态安全性
        if(Util.checkSecurity(processList,staticAvailable,new ArrayList<String>())){
            System.out.println("初始状态安全！");
            System.out.println("当前可调用资源量：");
            for(int i=0;i<staticAvailable.length;i++){
                System.out.print(staticAvailable[i]+" ");
            }
            System.out.println("");
        }else{
            System.out.println("初始状态不安全！");
        }

        //模拟进程2发送请求向量(1,0,2)
        int request1[] = new int[]{1,0,2};
        process2.sendRequest(request1,staticAvailable,processList);
        System.out.println("");

        //模拟进程5发送请求向量(3,3,0)
        int request2[] = new int[]{3,3,0};
        process5.sendRequest(request2,staticAvailable,processList);
        System.out.println("");

        //模拟进程1请求向量(0,2,0)
        int request3[] = new int[]{0,2,0};
        process1.sendRequest(request3,staticAvailable,processList);
        System.out.println("");
    }
}

class Process{
    String name;
    int max[];
    int allocation[];
    int need[];
    public Process(String name,int max[],int allocation[],int need[]){
        this.name = name;
        this.max = max;
        this.allocation = allocation;
        this.need = need;
    }

    //进程发送请求向量
    void sendRequest(int request[],int available[],List<Process> processList){
        System.out.print(this.name+"发送请求向量！：");
        for(int i=0;i<request.length;i++){
            System.out.print(request[i]+" ");
        }
        System.out.println("");
        int flag = 0;
        //如果available的每一项都大于或等于request
        if(Util.isAGreaterThanB(available,request)){
            flag++;
        }else{
            System.out.println("请求向量发送失败！当前资源不满足请求！该请求加入等待队列中！");
        }
        //系统满足这次资源请求
        if(flag==1){
            //检查给予资源后系统是否仍然处于安全态
            int tempAvailable[] = Util.arrayMinus(available,request);
            int tempAllocation[] = Util.arrayPlus(allocation,request);
            int tempNeed[] = Util.arrayMinus(max,tempAllocation);
            List<Process> tempList = new ArrayList();
            for(int j=0;j<processList.size();j++){
                tempList.add(processList.get(j));
            }
            //如果当前need数组元素和为0，则代表此次资源请求后进程资源已满足最大需求
            if(Util.ArraySum(tempNeed)==0){
                tempAvailable = Util.arrayPlus(tempAvailable,max);
                tempList.remove(this);
            }else{
                Process tempProcess = new Process(this.name,this.max,tempAllocation,tempNeed);
                tempList.remove(this);
                tempList.add(tempProcess);
            }
            //检查此时系统是否安全
            if(Util.checkSecurity(tempList,tempAvailable,new ArrayList<String>())){
                System.out.println("当前系统安全！");
                available = tempAvailable;
                allocation = tempAllocation;
                need = tempNeed;
                if(Util.ArraySum(need)==0){
                    processList.remove(this);
                }
                System.out.println("当前可调用资源量：");
                for(int i=0;i<available.length;i++){
                    System.out.print(available[i]+" ");
                }
                Banker.staticAvailable = available;
                System.out.println("");
            }else{
                System.out.println("当前系统不安全！拒绝操作！");
            }
        }
    }
}

class Util{
    //检查此时系统是否安全并输出安全序列
    public static boolean checkSecurity(List<Process> processList,int available[],List<String> securityList){
        //获取进程数
        int processNum = processList.size();
        if(processNum==0){
            System.out.println("当前安全序列为：");
            for(String processName:securityList){
                System.out.print(processName+" ");
            }
            System.out.println("");
            return true;
        }
        //依次判断当前进程的need是否小于available
        for(int i=0;i<processNum;i++){
            Process process = processList.get(i);
            if(isAGreaterThanB(available,process.need)){
                //记录当前满足条件的进程
                securityList.add(process.name);
                List<Process> tempList = new ArrayList();
                for(int j=0;j<processNum;j++){
                    tempList.add(processList.get(j));
                }
                tempList.remove(process);
                if(checkSecurity(tempList,arrayPlus(arrayMinus(available,process.need),process.max),securityList)){
                    return true;
                }else{
                    continue;
                }
            }
        }
        return false;
    }


    //两个相同大小的整型数组相减
    public static int[] arrayMinus(int a[],int b[]){
        if(a.length!=b.length){
            System.out.println("a b数组长度不符合，无法调用此方法！");
            throw new IllegalArgumentException("a b数组长度不符合，无法调用此方法！");
        }
        int[] tempArray = new int[a.length];
        for(int i=0;i<tempArray.length;i++){
            int tempValue = a[i]-b[i];
            tempArray[i] = tempValue;
        }
        return tempArray;
    }

    //两个相同大小的整形数组相加
    public static int[] arrayPlus(int a[],int b[]){
        if(a.length!=b.length){
            System.out.println("a b数组长度不符合，无法调用此方法！");
            throw new IllegalArgumentException("a b数组长度不符合，无法调用此方法！");
        }
        int[] tempArray = new int[a.length];
        for(int i=0;i<tempArray.length;i++){
            int tempValue = a[i]+b[i];
            tempArray[i] = tempValue;
        }
        return tempArray;
    }


    //判断a数组是否每一项都大于或等于b数组
    public static boolean isAGreaterThanB(int a[],int b[]){
        if(a.length!=b.length){
            System.out.println("a b数组长度不符合，无法调用此方法！");
            throw new IllegalArgumentException("a b数组长度不符合，无法调用此方法！");
        }
        for(int i=0;i<a.length;i++){
            if(a[i]<b[i]){
                return false;
            }
        }
        return true;
    }

    //求数列内元素和
    public static int ArraySum(int array[]){
        int sum = 0;
        for(int i=0;i<array.length;i++){
            sum+=array[i];
        }
        return sum;
    }
}
