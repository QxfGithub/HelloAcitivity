package com.qxf.acitivity;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstTest {

    /*创建流程引擎*/
    private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /*部署流程定义*/
    //执行后可以查询下面表  流程定义：act_re_procdef   所有流程：act_re_deployment    文件保存表：act_ge_bytearray
    @Test
    public void test1() {
        Deployment deployment = processEngine.getRepositoryService()// 获取部署相关Service
                .createDeployment()//创建部署
                .name("产品审核流程")//声明流程的名称
                .addClasspathResource("process/FirstBPMN.bpmn")//加载资源文件
                .deploy();
        System.out.println("流程部署ID:" + deployment.getId());
        System.out.println("流程部署Name:" + deployment.getName());
    }


    /*
     * 启动流程
     * 两种启动方式1 通过key  2 通过id
     * 我们通过key的方式启动 key为act_re_procdef表中的key字段值
     * */
    @Test
    public void startTask() {
        try {
            /*定义参数*/
            Map<String, Object> variables = new HashMap<String, Object>();
            //variables.put("productUsers", "zhangsan,lisi");//为candidateUsers的参数productUsers 指定值，用英文逗号分隔

            variables.put("requireGroup", "zhangsan");//为candidateUsers的参数productUsers 指定值，用英文逗号分隔

            ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey("myProcess_1",variables);
            System.out.println("流程实例ID:" + pi.getId());
            System.out.println("流程定义ID:" + pi.getProcessDefinitionId());

            //节点待办任务表 ：act_ru_task 会增加一条待办任务
            //现在待办任务已经形成，此条待办任务属于组任务的一种，通过组任务方法查询该任务

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //根据用户查询组待办任
    @Test
    public void test3() {
        String candidateUser = "zhangsan";
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskCandidateUser(candidateUser)// 组任务的办理人查询，不是组成员，查询不到
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                .list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        }
    }
    //我们可以看到任务办理人是为null，那么我们就可以在介绍一下组待办任务的认领，我现在指定了zhangsan,lisi两个审核人，但是此节点只需要一个人审批就可以了，组任务需要先认领，在办理
    //组待办任务认领，和解除认领


    /** 组代办任务认领*/   //认领后可以查询到个人任务 test4()
    @Test
    public void claim() {
        //任务ID
        String taskId = "2505";
        //认领人
        String userId = "zhangsan";
        processEngine.getTaskService().claim(taskId, userId);
    }

    /** 解除任务认领*/
    @Test
    public void setAssignee() {
        //任务ID
        String taskId = "2505";
        processEngine.getTaskService().setAssignee(taskId, null);
    }


    /** 查询当前人的个人任务 */
    @Test
    public void test4() {
        String assignee = "zhangsan";
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()//创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee(assignee)// 指定个人任务查询，指定办理人
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        }
    }



    //完成待办任务
    //完成任务时我们需要考虑下一个节点任务信息，根据流程图，产品审核的下一节点是需求审核，需求审核指定的是组审核

    /**
     * 完成任务
     */
    @Test
    public void test5() {
        TaskService taskService = processEngine.getTaskService();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("requireGroup", "requireGroup");//用户组，requireGroup
        //添加完成任务信息，可以不要
        //taskId,processInstance,message
        taskService.addComment("2505", "2501", "产品审核同意--zhangsan");
        //完成任务方法
        taskService.complete("2505", variables);
    }
    //现在act_ru_task表中产品审核节点已经不存在，新增需求审核节点数据
    //因为我们有了组，所以需要把用户和组的信息添加进去
    // 用户信息表：act_id_user 增加用户 id : zhangsan lisi wangwu
    //用户组信息表：act_id_group   增加组 ID : requireGroup
    //用户与用户组对应信息表：act_id_membership


    /*
     * 根据用户组名查询
     * */
    @Test
    public void test6() {
        //组代办
        List<Task> list = processEngine.getTaskService().createTaskQuery().taskCandidateGroup("requireGroup").list();
        for (Task task : list) {
            System.out.println("任务ID:" + task.getId());
            System.out.println("任务名称:" + task.getName());
            System.out.println("任务创建时间:" + task.getCreateTime());
            System.out.println("任务委派人:" + task.getAssignee());
            System.out.println("流程实例ID:" + task.getProcessInstanceId());
            System.out.println("########################################################");
        }
    }


    /**
     * 部署流程定义 zip
     */
    /*@Test
    public void deploymentProcessDefinition_zip() {
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("diagrams/helloworld.zip");
        ZipInputStream zipInputStream = new ZipInputStream(in);
        Deployment deployment = processEngine.getRepositoryService()// 与流程定义和部署对象相关的service
                .createDeployment()// 创建一个部署对象
                .name("流程定义")// 添加部署
                .addZipInputStream(zipInputStream)// 指定zip格式的文件完成部署
                .deploy();// 完成部署
        System.out.println("部署ID：" + deployment.getId());
        System.out.println("部署名称:" + deployment.getName());

    }*/


    /**
     * 查询所有的流程定义
     */
    /*@Test
    public void findProcessDefinition() {
        List<ProcessDefinition> list = processEngine.getRepositoryService()// 与流程定义和部署对象先相关的service
                .createProcessDefinitionQuery()// 创建一个流程定义的查询
                *//** 指定查询条件，where条件 *//*
                // .deploymentId(deploymentId) //使用部署对象ID查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                // .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                *//* 排序 *//*
                .orderByProcessDefinitionVersion().asc()
                // .orderByProcessDefinitionVersion().desc()

                *//* 返回的结果集 *//*
                .list();// 返回一个集合列表，封装流程定义
        // .singleResult();//返回惟一结果集
        // .count();//返回结果集数量
        // .listPage(firstResult, maxResults);//分页查询

        if (list != null && list.size() > 0) {
            for (ProcessDefinition pd : list) {
                System.out.println("流程定义ID:" + pd.getId());// 流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:" + pd.getName());// 对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:" + pd.getKey());// 对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:" + pd.getVersion());// 当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:" + pd.getResourceName());
                System.out.println("资源名称png文件:" + pd.getDiagramResourceName());
                System.out.println("部署对象ID：" + pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
    }*/


    /**
     * 查看流程图
     */
    /*@Test
    public void viewPic() throws IOException {
        // 将生产的图片放到文件夹下
        String deploymentId = "401";// TODO
        // 获取图片资源名称
        List<String> list = processEngine.getRepositoryService()
                .getDeploymentResourceNames(deploymentId);

        // 定义图片资源名称
        String resourceName = "";
        if (list != null && list.size() > 0) {
            for (String name : list) {
                if (name.indexOf(".png") >= 0) {
                    resourceName = name;
                }
            }
        }

        // 获取图片的输入流
        InputStream in = processEngine.getRepositoryService()
                .getResourceAsStream(deploymentId, resourceName);

        File file = new File("D:/" + resourceName);
        // 将输入流的图片写到D盘下
        FileUtils.copyInputStreamToFile(in, file);
    }*/




    //流程定义的删除，因为流程定义可以启动，所以涉及到一个普通删除和级联删除的情况，如果该流程定义下没有正在运行的流程，则可以用普通删除。
    // 如果是有关联的信息，用级联删除。关于删除我们既可以通过部署对象的id删除也可以通过流程定义的key删除，不同是使用id删除的只是一条记录，
    // 而使用key删除的是将key相同的所有版本的流程定义全部删除。

    /**
     * 删除流程定义(删除key相同的所有不同版本的流程定义)
     */
//    @Test
//    public void delteProcessDefinitionByKey() {
//        // 流程定义的Key
//        String processDefinitionKey = "HelloWorld";
//        // 先使用流程定义的key查询流程定义，查询出所有的版本
//        List<ProcessDefinition> list = processEngine.getRepositoryService()
//              .createProcessDefinitionQuery()
//                .processDefinitionKey(processDefinitionKey)// 使用流程定义的key查询
//                .list();
//        // 遍历，获取每个流程定义的部署ID
//        if (list != null && list.size() > 0) {
//            for (ProcessDefinition pd : list) {
//                // 获取部署ID
//                String deploymentId = pd.getDeploymentId();
//                //		/*
//                //		 * 不带级联的删除， 只能删除没有启动的流程，如果流程启动，就会抛出异常
//                //		 */
//                //		 processEngine.getRepositoryService().deleteDeployment(deploymentId);
//
//                /**
//                 * 级联删除 不管流程是否启动，都可以删除
//                 */
//                processEngine.getRepositoryService().deleteDeployment(
//                        deploymentId, true);
//
//            }
//
//            }
////    }

    /**
     * 查询历史流程实例
     */
    @Test
    public void findHistoryProcessInstance(){
        String processInstanceId="2501";
        HistoricProcessInstance hpi = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        System.out.println(hpi.getId() +"    "+hpi.getProcessDefinitionId()+"   "+ hpi.getStartTime()+"   "+hpi.getDurationInMillis());
    }

    /**
     * 查询流程状态（判断流程正在执行，还是结束）
     */
    @Test
    public void isProcessEnd(){
        String processInstanceId =  "2501";
        ProcessInstance pi = processEngine.getRuntimeService()//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();

        if(pi==null){
            System.out.println("流程已经结束");
        }
        else{
            System.out.println("流程没有结束");
        }

    }


}
