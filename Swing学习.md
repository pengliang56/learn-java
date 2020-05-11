







### JFrame

构造函数参数 title 为窗口标题

```
JFrame frame = new JFrame("测试JFrame");
```

设置窗口大小

```
frame.setBounds(300, 300, 600, 500);
```

设置窗口关闭程序结束,默认关闭窗口程序不会结束

```
frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
```

设置窗口可见

```
frame.setVisible(true);
```

### JLabel

设置水平对齐

```
label.setHorizontalAlignment(SwingConstants.CENTER);
```

设置文本

```
label.setText(value);
```

设置边框

```
label.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
```

设置背景是否透明

```
label.setOpaque(true);
```

设置字体

```
label.setFont();
```

### JList

```
JList<String> showPerson = new JList<>();
```

JList 的数据模型 DefaultListModel ,JList会调用模型的 toString 方法填充数据

```
DefaultListModel<String> personModel = new DefaultListModel<>();
```

通过 setModel() 方法设置到列表控件

列表可以设置成单选模式,默认是多选

```
showPerson.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
```

列表一般会设置成滚动模式,可以通过构造函数加入到滚动控件中

也可以通过 setViewportView() 方法加入滚动控件中

```
JScrollPane scrollPane = new JScrollPane(new JList());
```

//  ListCellRenderer 

可以通过其他控件绘制图表,需要实现这个类,并返回控件

```java
/** 
* @param list 列表控件 
* @param value 列表项的值 
* @param index 索引 
* @param isSelected 该项是否被选中 
* @param cellHasFocus 该项是否是焦点 
*/
public Component getListCellRendererComponent(
    JList<? extends String> list, 
    String value, int index, 
    boolean isSelected, 
    boolean cellHasFocus) 
```

Jlist 控件也可以布局复杂的画面, 但是只是绘制图表,不会创建很多控件,比如 button 只会有一个画面,添加事件会失效,具体怎么解决呢?这是个疑问



### JTable

数据模型

DefaultTableModel 数据是双向的

 只有获取到表格的列后才能添加渲染器。 

 his.table.getColumnModel().getColumn(2).setCellRenderer(new MyButtonRender()); 

### Jtable 添加 Jbutton

 https://blog.csdn.net/u011393661/article/details/39505009?utm_medium=distribute.pc_relevant_right.none-task-blog-BlogCommendFromMachineLearnPai2-9.nonecase&depth_1-utm_source=distribute.pc_relevant_right.none-task-blog-BlogCommendFromMachineLearnPai2-9.nonecase 



 https://docs.oracle.com/javase/tutorial/uiswing/components/table.html 

Jtable 去掉边框(只是不显示边框,但是还是有的)

```java
table.setShowGrid(false); // 整个去掉不可用
table.setShowHorizontalLines(false); // 水平线去掉
table.setShowVerticalLines(false); // 垂直线去掉

// 能控制几行几列去掉
DefaultTableModel model = new DefaultTableModel()/*{
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}*/
```

jtable 获取焦点时候,去掉高亮

```java
public class MyRenderer extends DefaultTableCellRenderer {    
    @Override    
    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value, 
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, int column) {       			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);     setBorder(noFocusBorder);       
    return this;                                                                   
	}
}
```

去白边研究