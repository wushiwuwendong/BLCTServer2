package cn.bw.looters.ms.web.ctrl;

import cn.bw.looters.ms.biz.facade.MsFacade;
import cn.bw.looters.ms.web.vo.req.TreeNodesRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Set;
import java.util.TreeSet;


@Controller
@RequestMapping("/ms")
public class MsCtrl {
    @RequestMapping("/")
    public String index() {
        return "index";
    }


    /**
     * 登录操作
     */
    @RequestMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        LOGGER.info("{}-{}", username, password);
        return "main";
    }

    /**
     * TODO - zhYou: 修改返回数据, 与使其继承自Res对象; 查看TreeLoader中的配置
     *
     * @return 树节点配置
     */
    @ResponseBody
    @RequestMapping("/mainTreeNodes")
    public Set<TreeNodesRes.TreeNode> mainTreeNodes() {
        Set<TreeNodesRes.TreeNode> nodes = new TreeSet<TreeNodesRes.TreeNode>();

        // 管理平台
        TreeNodesRes.TreeNode ms = new TreeNodesRes.TreeNode(1000, "平台", false);
        ms.addChildren(new TreeNodesRes.TreeNode(1001, "用户", true));
        ms.addChildren(new TreeNodesRes.TreeNode(1002, "权限", true));
        nodes.add(ms);

        // 小小战争
        TreeNodesRes.TreeNode looters = new TreeNodesRes.TreeNode(2000, "强拆骑士团", false);
        TreeNodesRes.TreeNode confNode = new TreeNodesRes.TreeNode(2100, "配置", false);
        confNode.addChildren(new TreeNodesRes.TreeNode(2101, "建筑信息", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2102, "建筑等级", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2103, "角色信息", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2104, "角色等级", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2105, "服务器", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2106, "服务器常数", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2107, "用户初始化", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2108, "加速消费", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2109, "商店兑换", true));
        confNode.addChildren(new TreeNodesRes.TreeNode(2110, "角色等级", true));

        TreeNodesRes.TreeNode dataNode = new TreeNodesRes.TreeNode(2200, "数据", false);
        dataNode.addChildren(new TreeNodesRes.TreeNode(2201, "角色", true));
        dataNode.addChildren(new TreeNodesRes.TreeNode(2202, "黑名单", true));
        dataNode.addChildren(new TreeNodesRes.TreeNode(2203, "宝石", true));
        dataNode.addChildren(new TreeNodesRes.TreeNode(2204, "平台", true));

        looters.addChildren(confNode);
        looters.addChildren(dataNode);
        nodes.add(looters);

        return nodes;
        // TreeNodesRes res = new TreeNodesRes();
        // res.setNodes(nodes);
        // return res;

    }

    @Resource
    private MsFacade msFacade;
    private static final Logger LOGGER = LoggerFactory.getLogger(MsCtrl.class);
}