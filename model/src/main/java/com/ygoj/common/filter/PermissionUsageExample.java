package com.ygoj.common.filter;

/**
 * 权限注解使用示例
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
public class PermissionUsageExample {
    
    // ==================== 示例1: 角色权限验证 ====================
    
    /**
     * 仅管理员可访问
     */
    // @Permission(type = Permission.PermissionType.ROLE, value = "ADMIN")
    // public Result adminOnly() { ... }
    
    /**
     * 管理员或比赛管理员可访问（OR逻辑）
     */
    // @Permission(
    //     type = Permission.PermissionType.ROLE, 
    //     value = "ADMIN",
    //     extra = {"CONTEST_ADMIN"},
    //     logical = Permission.Logical.OR
    // )
    // public Result adminOrContestAdmin() { ... }
    
    // ==================== 示例2: 位运算权限验证 ====================
    
    /**
     * 需要"查看题目"权限（权限位0）
     */
    // @Permission(
    //     type = Permission.PermissionType.BIT, 
    //     value = "0"
    // )
    // public Result viewProblem() { ... }
    
    /**
     * 需要"创建题目"或"编辑题目"权限（权限位2或3）
     */
    // @Permission(
    //     type = Permission.PermissionType.BIT, 
    //     value = "2",
    //     extra = {"3"},
    //     logical = Permission.Logical.OR
    // )
    // public Result createOrEditProblem() { ... }
    
    /**
     * 需要同时拥有"创建题目"和"编辑题目"权限（权限位2和3）
     */
    // @Permission(
    //     type = Permission.PermissionType.BIT, 
    //     value = "2",
    //     extra = {"3"},
    //     logical = Permission.Logical.AND
    // )
    // public Result createAndEditProblem() { ... }
    
    // ==================== 示例3: 自定义权限验证 ====================
    
    /**
     * 需要"problem:create"权限
     */
    // @Permission(
    //     type = Permission.PermissionType.CUSTOM, 
    //     value = "problem:create"
    // )
    // public Result createProblem() { ... }
    
    /**
     * 需要"problem:create"或"problem:edit"权限
     */
    // @Permission(
    //     type = Permission.PermissionType.CUSTOM, 
    //     value = "problem:create",
    //     extra = {"problem:edit"},
    //     logical = Permission.Logical.OR
    // )
    // public Result createOrEditProblemCustom() { ... }
    
    // ==================== 示例4: 不需要登录的接口 ====================
    
    /**
     * 公开接口，无需登录
     */
    // @Permission(requireLogin = false)
    // public Result publicApi() { ... }
    
    // ==================== 示例5: 类级别权限 ====================
    
    /**
     * 在Controller类上添加权限，所有方法都需要该权限
     */
    // @Permission(type = Permission.PermissionType.ROLE, value = "ADMIN")
    // @RestController
    // @RequestMapping("/admin")
    // public class AdminController {
    //     // 所有方法都需要ADMIN角色
    //     
    //     // 但可以为特定方法覆盖权限
    //     @Permission(type = Permission.PermissionType.ROLE, value = "SUPER_ADMIN")
    //     public Result superAdminOnly() { ... }
    // }
    
    // ==================== 示例6: 实际业务场景 ====================
    
    /**
     * 场景1: 普通用户可以查看题目
     */
    // @Permission(
    //     type = Permission.PermissionType.BIT, 
    //     value = "0", // PERM_PROBLEM_VIEW
    //     message = "您没有查看题目的权限"
    // )
    // @GetMapping("/problem/{id}")
    // public Result getProblem(@PathVariable Long id) { ... }
    
    /**
     * 场景2: 只有登录后才能提交代码
     */
    // @Permission(
    //     type = Permission.PermissionType.BIT, 
    //     value = "1", // PERM_PROBLEM_SUBMIT
    //     message = "请先登录后再提交代码"
    // )
    // @PostMapping("/submit")
    // public Result submitCode(@RequestBody SubmitRequest request) { ... }
    
    /**
     * 场景3: 只有题目管理员可以创建题目
     */
    // @Permission(
    //     type = Permission.PermissionType.ROLE, 
    //     value = "PROBLEM_ADMIN",
    //     extra = {"ADMIN"},
    //     logical = Permission.Logical.OR,
    //     message = "只有题目管理员可以创建题目"
    // )
    // @PostMapping("/problem/create")
    // public Result createProblem(@RequestBody Problem problem) { ... }
    
    /**
     * 场景4: 比赛期间才允许访问（需要在拦截器中额外处理）
     */
    // @Permission(
    //     type = Permission.PermissionType.BIT, 
    //     value = "11", // PERM_CONTEST_JOIN
    //     message = "您没有参加该比赛的权限"
    // )
    // @GetMapping("/contest/{id}/problems")
    // public Result getContestProblems(@PathVariable Long id) { ... }
}
