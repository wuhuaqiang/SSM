package com.kylin.electricassistsys.systemlogaspect;

import com.kylin.electricassistsys.data.api.other.SysSystemsettingApi;
import com.kylin.electricassistsys.data.api.system.SysLoginStatusDataApi;
import com.kylin.electricassistsys.data.api.tsys.TSysModuleDataApi;
import com.kylin.electricassistsys.data.api.tsys.TSystemLogApi;
import com.kylin.electricassistsys.dto.base.BaseDto;
import com.kylin.electricassistsys.dto.other.SysSystemsettingDto;
import com.kylin.electricassistsys.dto.system.SysLoginStatusDto;
import com.kylin.electricassistsys.dto.system.TSystemLogDto;
import com.kylin.electricassistsys.dto.tsbsj.TSbsjBdzxxSelDto;
import com.kylin.electricassistsys.dto.tsys.TSysModuleDto;
import com.kylin.electricassistsys.dto.wghgl.TDwghglXmqcSelDto;
import com.kylin.electricassistsys.mybeanutils.JSONResult;
import com.kylin.electricassistsys.redisutils.RedisCacheService;
import com.kylin.electricassistsys.rsas.RSATools;
import com.kylin.electricassistsys.tools.IPHelper;
import com.kylin.electricassistsys.tools.json.JsonUtils;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.UnauthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

/***
 * 系统操作日志记录数据
 * spring aop 采用的是 cjlib
 * cwx
 * 2018/5/11
 */

@Aspect    //该标签把LoggerAspect类声明为一个切面
@Order(1)  //设置切面的优先级：如果有多个切面，可通过设置优先级控制切面的执行顺序（数值越小，优先级越高）
@Component //该标签把LoggerAspect类放到IOC容器中
public class SystemLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(SystemLogAspect.class);
    @Resource
    private TSystemLogApi tSystemLogApi;
    @Resource
    private SysLoginStatusDataApi sysLoginStatusDataApi;
    @Resource
    private SysSystemsettingApi systemsettingApi;
    @Resource
    private TSysModuleDataApi tSysModuleDataApi;
    @Resource
    private RedisCacheService redisCacheService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private Map<String, List<String>> userUrlMap = new HashMap<String, List<String>>();
    private Map<String, String> moduleMap = new HashMap<String, String>();


    /**
     * 环绕通知(需要携带类型为ProceedingJoinPoint类型的参数)
     * 环绕通知包含前置、后置、返回、异常通知；ProceedingJoinPoin 类型的参数可以决定是否执行目标方法
     * 且环绕通知必须有返回值，返回值即目标方法的返回值
     *
     * @param
     */
    @Around("@within(org.springframework.web.bind.annotation.RequestMapping)")
    public Object aroundMethod(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPHelper.getIpAddress(request);
        List<TSysModuleDto> tSysModuleDtos = tSysModuleDataApi.getList();
        for (TSysModuleDto tSysModuleDto : tSysModuleDtos) {
            moduleMap.put(tSysModuleDto.getModuleCode(), tSysModuleDto.getModuleName());
        }
        Object o = null;
        SysLoginStatusDto sysLoginStatusDto = null;
        String sessionId = null;
        long t1 = System.currentTimeMillis();
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof Map) {
                Map<String, Object> param = (Map<String, Object>) args[0];
                sessionId = (String) param.get("userRedisreQequestId");
            }
            Cookie[] cookie = request.getCookies();
            if (cookie != null) {
                for (int i = 0; i < cookie.length; i++) {
                    Cookie cook = cookie[i];
                    if (cook.getName().equalsIgnoreCase("Admin-Token")) { //获取键
                        sessionId = cook.getValue().toString();//获取值
                    }
                }
            }
            if (sessionId != null) {
                sysLoginStatusDto = sysLoginStatusDataApi.selectSysLoginStatusBySessionId(sessionId);
            }
            TSystemLogDto dto = new TSystemLogDto();
            if (sysLoginStatusDto != null) {
                String userId = sysLoginStatusDto.getSysUserId().toString();
                dto.setUserId(userId.substring(0, userId.indexOf('.')));
                dto.setUserName(sysLoginStatusDto.getSysUserLoginName());
            }
            o = point.proceed();
            long t2 = System.currentTimeMillis();
         /*   if (o.toString().length() < 5000 || o.toString().length() != 0) {
                dto.setUserResult(o.toString());
            } else {
                dto.setUserResult("data is too long");
            }*/
            JSONResult result = null;
            if (o instanceof JSONResult) {
                result = (JSONResult) o;
                if (result != null && result.getCode().getCode() == "0") {
                    dto.setDataStatus(1L);
                } else {
                    dto.setDataStatus(0L);
                }
                dto.setRemark(result.getMsg());
            }
            dto.setUserDuration((t2 - t1));
            dto.setUserMethod(point.getTarget().getClass().getName() + "." + point.getSignature().getName());
            /*StringBuilder stringBuilder = new StringBuilder();
            for (String s : request.getParameterMap().keySet()) {
                stringBuilder.append(s);
                stringBuilder.append(" = ");
                stringBuilder.append(request.getParameterMap().get(s)[0]);
                stringBuilder.append(" | ");
            }*/
//            dto.setUserParameters(stringBuilder.toString());
            dto.setUserIp(ipAddress);
            dto.setUserURL(request.getRequestURL().toString());
            String requestUrl = request.getRequestURL().toString();
            String requestUrl_lower = request.getRequestURL().toString().toLowerCase();
            if (requestUrl_lower.contains("select") || requestUrl_lower.contains("query") || requestUrl_lower.contains("list") || requestUrl_lower.contains("page") || requestUrl_lower.contains("get")) {
                if (requestUrl_lower.contains("getuserlogin")) {
                    dto.setLogType("登录");
                    if (result != null && result.getCode().getCode() == "0") {
                        String data = result.getData().toString();
                        Map<String, Object> resultMap = JsonUtils.json2Map(data);
                        String loginName = (String) resultMap.get("loginName");
                        String userId = ((Integer) resultMap.get("id")).toString();
                        dto.setUserName(loginName);
                        dto.setUserId(userId);
                    } else {
                        if (args != null && args.length > 0 && args[0] instanceof Map) {
                            Map<String, Object> param = (Map<String, Object>) args[0];
                            String loginName = RSATools.decryptDataOnJava((String) param.get("loginName"));
                            dto.setUserName(loginName);
                        }
                    }
                } else if (requestUrl_lower.contains("logout")) {
                    dto.setLogType("登出");
                    dto.setDataStatus(1L);
                    dto.setRemark("登出成功");
                } else {
                    dto.setLogType("查询");
                }

            } else if (requestUrl_lower.contains("insert") || requestUrl_lower.contains("add")) {
                dto.setLogType("新增");
            } else if (requestUrl_lower.contains("update") || requestUrl_lower.contains("updata") || requestUrl_lower.contains("set")) {
                dto.setLogType("更新");
            } else if (requestUrl_lower.contains("delete") || requestUrl_lower.contains("del")) {
                dto.setLogType("删除");
            } else {
                dto.setLogType("查询");
                dto.setModuleName("首页统计查询");
            }
            if (request.getRequestURL().toString().contains("permissionSystem") || request.getRequestURL().toString().contains("systemsetting")) {
                dto.setEventType("系统事件");
                Object[] params = point.getArgs();
                if (params != null && params.length > 0 && params[0] instanceof Map) {
                    Map<String, Object> param = (Map<String, Object>) params[0];
                    String moduleCode = param.get("moduleCode").toString();
                    String moduleName = moduleMap.get(moduleCode);
                    if ("登录".equals(dto.getLogType()) || "登出".equals(dto.getLogType())) {
                        dto.setModuleName(moduleName);
                    } else {
                        dto.setModuleName(moduleName + dto.getLogType());
                    }

                } else {
                    if (requestUrl_lower.contains("captchimg")) {
                        dto.setModuleName("验证码查询");
                        dto.setDataStatus(1L);
                        dto.setRemark("获取成功");
                    } else {
                        String moduleName = moduleMap.get("systemSetting");
                        dto.setModuleName(moduleName + dto.getLogType());
                    }
                }
            } else {
                dto.setEventType("业务事件");
                int i = requestUrl.lastIndexOf("//");
                String moduleCode = requestUrl.substring(requestUrl.lastIndexOf("//") + 2, requestUrl.lastIndexOf("/"));
                if (moduleCode != null && "resourcemanagement".equals(moduleCode)) {
                    moduleCode = requestUrl.substring(requestUrl.lastIndexOf("/") + 1);
                    String moduleName = moduleMap.get(moduleCode);
                    dto.setModuleName(moduleName + dto.getLogType());
                } else if (moduleCode != null && "glxmqc".equals(moduleCode)) {
                    Object[] params = point.getArgs();
                    TDwghglXmqcSelDto xmqc = (TDwghglXmqcSelDto) params[0];
                    String moduleName = xmqc.gettXmqcType();
                    dto.setModuleName(moduleName + dto.getLogType());
                } else if (moduleCode != null && "bdzxx".equals(moduleCode)) {
                    Object[] params = point.getArgs();
                    TSbsjBdzxxSelDto bdzxx = (TSbsjBdzxxSelDto) params[0];
                    String s = bdzxx.gettBdzxxDydj();
                    if (s != null) {
                        moduleCode = "kbs";
                    }
                    String moduleName = moduleMap.get(moduleCode);
                    dto.setModuleName(moduleName + dto.getLogType());
                } else {
                    String moduleName = moduleMap.get(moduleCode);
                    dto.setModuleName(moduleName + dto.getLogType());
                }
            }

            dto.setUserAgent(request.getHeader("user-agent"));
            tSystemLogApi.insertSystem(dto);
            logger.info("request contentType:{}", request.getHeader("Accept"));
            logger.info("request param : {}", dto.getUserParameters());
            logger.info("reuest method : {}", request.getMethod());
            logger.info("request url : {}", dto.getUserURL());
        } catch (Exception e) {//这里建议将异常向上层抛让异常处理器来进行捕捉
            if (e instanceof UnknownAccountException) {
                throw new UnknownAccountException(e);
            } else if (e instanceof IncorrectCredentialsException) {
                throw new IncorrectCredentialsException(e);
            } else if (e instanceof UnauthorizedException) {
                throw new UnauthorizedException(e);
            } else if (e instanceof ExcessiveAttemptsException) {
                throw e;
            } else {
                throw new Exception(e);
            }
        }
        return o;
    }


    /***
     * 定义一个方法，用于声明切入点表达式，方法中一般不需要添加其他代码
     * 使用@Pointcut声明切入点表达式
     * 后面的通知直接使用方法名来引用当前的切点表达式；如果是其他类使用，加上包名即可
     */

  /*  @Pointcut("execution(public * com.kylin.electricassistsys.controller.*Controller.*(..))")
    public void declearJoinPointExpression(){}
*/
    @Pointcut("execution(public * com.kylin.electricassistsys.controller.*.*Controller.*(..))")
    public void declearJoinPointExpression() {
    }

    /***
     * 前置通知
     * @param joinPoint
     *
     * */


    @Before("declearJoinPointExpression()") //该标签声明次方法是一个前置通知：在目标方法开始之前执行
    public void beforMethod(JoinPoint joinPoint) {
        // String url = request.getRequestURI();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        String url = request.getRequestURI().substring(1);
        List<SysSystemsettingDto> sysSystemsettingDtos = systemsettingApi.selectSystemsettingResult();
        if (sysSystemsettingDtos.size() > 0 && !"/permissionSystem/user/getUserLogin".equals(url) && !"/permissionSystem/system/captchImg".equals(url)) {
            SysSystemsettingDto sysSystemsettingDto = sysSystemsettingDtos.get(0);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date visitstarttime = sysSystemsettingDto.getVisitstarttime();
            Date visitendtime = sysSystemsettingDto.getVisitendtime();
            if (visitstarttime != null || visitendtime != null) {
                String visitstarttimeStr = sdf.format(visitstarttime);
                String visitendtimeStr = sdf.format(visitendtime);
                LocalTime now = LocalTime.now();
                String s = now.toString();
                String nowStr = s.substring(0, s.indexOf("."));
                long visitstarttimeLon = Long.valueOf(visitstarttimeStr.replaceAll("[-\\s:]", ""));
                long visitendtimeLon = Long.valueOf(visitendtimeStr.replaceAll("[-\\s:]", ""));
                long nowLon = Long.valueOf(nowStr.replaceAll("[-\\s:]", ""));
                Boolean result = visitstarttimeLon > nowLon || nowLon > visitendtimeLon;
                if (result) {
                    response.setStatus(400);
                    try {
                        response.getWriter().write(":" + visitstarttimeStr + "-" + visitendtimeStr + "*时间段才能被访问！");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    throw new ExcessiveAttemptsException("系统管理设置系统在:" + visitstarttimeStr + "-" + visitendtimeStr + "时间段才能被访问！");
                }
            }
        }


        if (url.indexOf("/permissionSystem") == -1) {
            String methodName = joinPoint.getSignature().getName();
            List<Object> args = Arrays.asList(joinPoint.getArgs());

            if (args.size() > 0) {
                String userRedisreQequestId = "";
                if (args.get(0) instanceof Map) {
                    Map map = (Map) args.get(0);
                    userRedisreQequestId = (String) map.get("userRedisreQequestId");
                } else {
                    if (args.get(0) instanceof BaseDto) {
                        BaseDto baseDto = (BaseDto) args.get(0);
                        userRedisreQequestId = baseDto.getUserRedisreQequestId();
                    } else {
                        Cookie[] cookie = request.getCookies();
                        for (int i = 0; i < cookie.length; i++) {
                            Cookie cook = cookie[i];
                            if (cook.getName().equalsIgnoreCase("Admin-Token")) { //获取键
                                userRedisreQequestId = cook.getValue().toString();    //获取值
                            }
                        }
                    }
                }
                if (userRedisreQequestId != null) {
                    boolean faleg = redisCacheService.hasKey(userRedisreQequestId);
                    if (!faleg) {
                        response.setStatus(410);
                        try {
                            response.getWriter().write(":-*登录验证码过期！");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        throw new ExcessiveAttemptsException("登录验证码过期！");
                    }
                }

                if (userRedisreQequestId != null && userUrlMap.get(userRedisreQequestId) == null) {
                    List<String> requestUrlSet = new ArrayList<String>();
                    String key = userRedisreQequestId + "_Permission";
                    Boolean aBoolean = redisTemplate.opsForSet().isMember(key, url);
                    Set<Object> members = redisTemplate.opsForSet().members(key);
                    Iterator<Object> iterator = members.iterator();
//                    requestUrlSet = new ArrayList<String>();
                    while (iterator.hasNext()) {
                        String s = iterator.next().toString();
                        int api = s.indexOf("api");
                        if (api != -1) {
                            String requestUrl = s.substring(api + 3);
                            int i = requestUrl.indexOf("_");
                            if (i != -1) {
                                requestUrl.replaceAll("_", "*");
                                String[] split = requestUrl.split("_");
                                requestUrl = new StringBuilder(split[0]).append(split[1]).toString();
                            }
                            requestUrlSet.add(requestUrl);
                        }
                        userUrlMap.put(userRedisreQequestId, requestUrlSet);
                    }

                }
                if (userUrlMap.get(userRedisreQequestId).size() > 0 && !userUrlMap.get(userRedisreQequestId).contains(url)) {
                    response.setStatus(403);
                    throw new ExcessiveAttemptsException("没有权限访问！");
                }
            }
        }
        //  System.out.println("this method " + methodName + " begin. param<" + args + ">");
    }

    /***
     * 后置通知（无论方法是否发生异常都会执行,所以访问不到方法的返回值）
     * @param joinPoint
     * */


    @After("declearJoinPointExpression()")
    public void afterMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("this method " + methodName + " end.");
    }

    /***
     * 返回通知（在方法正常结束执行的代码）
     * 返回通知可以访问到方法的返回值！
     * @param
     */

    @AfterReturning(value = "declearJoinPointExpression()", returning = "result")
    public void afterReturnMethod(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("this method " + methodName + " end.result<" + result + ">");
    }

    /***
     * 异常通知（方法发生异常执行的代码）
     * 可以访问到异常对象；且可以指定在出现特定异常时执行的代码
     * @param joinPoint
     * @param ex
     * */


    @AfterThrowing(value = "declearJoinPointExpression()", throwing = "ex")
    public void afterThrowingMethod(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("this method " + methodName + " end.ex message<" + ex + ">");
    }

}
