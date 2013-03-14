#notes:
#	1) generate input files: 
#		run sh mining/classify_obj_mean_size.sh /media/DisqueC/Temp/april_analysis/aren/general/output_dar_400req_40M_to_140/ /media/DisqueC/Temp/april_analysis/aren/general/output_lru_400req_40M_to_140/ /media/DisqueC/Temp/april_analysis/aren/general/output_aren_400req_40M_to_140/
#	2) run R --slave --no-save --no-restore --no-environ --silent < obj_mean_size_happiness.r 
#output preview in 
#		output/obj_mean_size_happiness.png 

##module
#this function assumes that values are in creasent order
format<-function(vector,factor)
{
	index<-1;
	for (x in vector) {
		vector[index]<-round(x/factor,digits=1)
		index<-index+1;
	}
	return(vector);
}

format2ChLabel<-function(ch1,ch2){
	label<-expression(paste(plain(ch1)[ch2], ""));
	return(label);
}

getLabel<-function(string){
	start_index<-2;
	last_index<-start_index;
	cur_index<-start_index;
	repeat{
		pos_num<-as.numeric(substr(string,cur_index,cur_index))
		if(is.na(pos_num)){
			break
		}
		last_index<-cur_index;
		cur_index<-cur_index+1;
	}
	label1<-substr(string,1,1)
	num<-as.numeric(substr(string,start_index,last_index))
	return(bquote(.(label1)[.(num)]));
	
}
#main

#l_labels <- 
#		read.table("formats/storage_happiness.txt",
#				header = TRUE,sep=",")


e1 <- c(8.5,6,5.3,5.1,5.1); #there are three line per column, the mean obj size changes per line as follows: 30, 45, 60
#1: 3540000,6.06,4726,7,54452392.00,8035655
#5.10017 (avg bwd)
#0.00282278 (msg duration)
#60.1806 (men_size)
#62.6844 (msg/s)
#
#1.0758: 3540000,4.91,4624,8,219057168.00,6482670
#5.12153
#0.00296444
#82.1126
#78.0547
#
#1.18: 3540000,5.63,7159,21,276733216.00,4998150
#5.31356
#0.00248951
#97.3945
#104.996
#
#1.37: 3540000,6.39,12069,20,221927592.00,3504494
#5.99373
#0.00226687
#136.828
#168.489
#
#
#2: 3540000,8.66,26041,47,314701392.00,2001385
#8.55356
#0.00210321
#229.142
#420.257
plot_colors <- colors()[c(556)]

pdf(file="output/lock_based_bwd.pdf", 
		height=6, width=10, bg = "transparent")

#png(filename="output/obj_mean_size_happiness.png", 
#		height=1600, width=3200, bg = "transparent",res=1200)

#setting maring for small figures
par(mar=c(3.2,3,0.1,.1)) #margins, c(bottom, left, top, right)
par(mgp=c(2,.8,0)) #positions of axis labels and titles, as well, axis lines
global_cex=2
par(cex=global_cex) #text length
#global_cex=.5
#global_lwd=.8
#par(mar=c(2.4,2.85,.4,0.05)) #margins, c(bottom, left, top, right)
#par(cex=global_cex) #text length
#par(cex.axis=.9) #axis width in relation to global cex
#par(lwd=global_lwd) #graphic lines
##x-axis and y-axis line width
#x_axis_lwd=global_lwd
#y_axis_lwd=global_lwd
#par(mgp=c(1.2,.6,0)) #positions of axis labels and titles, as well, axis lines
#par(lwd=.1)

#fine control of margens of a figure
#par(mar=c(2,2.8,.5,.5))

#for more colors, 
#see 
#http://research.stowers-institute.org/efg/R/Color/Chart/

# Graph autos using y axis that 
#ranges from 0 to max_y.
# Turn off axes and annotations 
#(axis labels) so we can 
# specify them ourself
y<-seq(0,10);
x<-seq(1,10);
#x<-seq(0,max_happiness,max_happiness/(length(y)-1))

x_at<-c(1,3.25,5.5,7.75,10)
x_labels<-c("2","3.5","5","6.5","8")

#plot(x, y, axes=F, type="n", 
#		xlab="Happiness (1000 clients and 400req/s)",
#		ylab="Storage Usage (TB)", ylim=c(0,max_y),
#		axes=FALSE, ann = FALSE)
plot(x, axes=F, type="n", ylim=c(0,10),
		ann = FALSE)

mtext(side = 2, text = "Average bandwidth (Mbps)", line = 2, cex=global_cex)#, cex=global_cex)#, cex=.5) #line modifies mtext placement
mtext(side = 1, text = "Content mean size (MB)", line = 2, cex=global_cex)#, cex=global_cex)#, cex=.5)

axis(1, at = x_at, 
		labels = x_labels,padj=-.5)#,cex.axis=.8,lwd=global_lwd)

# Make y axis with horizontal labels that display ticks at 
# every 4 marks. 4*0:max_y is equivalent to c(0,4,8,12).
axis(2, las=1, at=(0:(10)))#,cex.axis=.8,lwd=global_lwd)

lines(x_at,e1, type="o", pch=20, lty=1, 
		col=plot_colors[1])#, cex=.5);


# Create box around plot
box();

# Create a legend at (1, max_y) that is slightly smaller 
# (cex) and uses the same line colors and points used by 
# the actual plots
#legend("bottomleft",col=rev(plot_colors), 
#		text.col=rev(plot_colors),
#		pch=21:23, lty=1:3, 
#		c("AREN","caching","DAR"), 
#		bty="n")#, cex=.4);

#print out to file
dev.off();

#stop('break point');
#here below the legacy code

#################### LEGACY ########################"
## Graph trucks with red dashed line and square points
#lines(replicas_happiness, replicas_storage, type="o", pch=22, lty=1, col=plot_colors[1])
#lines(cache_happiness, cache_storage, type="o", pch=23, lty=2, col=plot_colors[2])
#
##lines(arentc, type="l", lty=3, col=plot_colors[3])
#
#index<-1;
#while (index<=length(replicas_storage)) {
#	storage<-replicas_storage[index];
#	happiness<-replicas_happiness[index]+(0.05*max(x));
#	text(happiness,storage, col=plot_colors[1], paste("r=",index,sep=""));
#	index<-index+1;
#}
#
#index<-1;
#while (index<=length(replicas_storage)) {
#	storage<-cache_storage[index];
#	happiness<-cache_happiness[index]-(0.20*max(x));
#	if(index==1){
#		text(happiness,storage, col=plot_colors[2], paste("LRU cache(size=1%)",sep=""),adj=c(0.5,.5));
#	} else if(index==2){
#		text(happiness,storage, col=plot_colors[2], paste("LRU cache(size=5%)",sep=""),adj=c(0.5,0.5));
#	} else if(index==3){
#		text(happiness,storage, col=plot_colors[2], paste("LRU cache(size=50%)",sep=""),adj=c(0.5,0.5));
#	}
#	index<-index+1;
#}
#
##print out to file
#dev.off();
#
#stop('break point');
##here below the legacy code
#
#index<-1;
#size<-0;
#happy_count<-0;
#pch_index<-0;
#color_index<-241;
#color_step<-20;
#end_list<-3;
#color_panel<-append(rep(24,length(storage)-end_list),rep(54,end_list))
#while (index<=length(storage)) {
#	size<-storage[index];
#	happy_count<-happiness[index];
#	points(happy_count, size, pch=pch_index+index, col=colors()[color_panel[index]]);
##	print("type")
##	print(type[index])
##	print("sub")
##	print(substr(type[index],1,1))
##	label<-format2ChLabel(substr(type[index],1,1),substr(type[index],2,2));
##	print("label")
##	print(label)
##	label<-expression(paste(substr(plain(type[index],1,1))[substr(type[index],2,2)],""))
##	start_index<-2;
##	last_index<-start_index;
##	cur_index<-start_index;
##	text(happy_count+shift_x,size+shift_y,type[index],col=colors()[color_panel[index]],cex=1.2)
##	label1<-"r"
##	l_index<-1
##	t_index<-"(1)";
#	#.(label1)[.(l_index)]),t_index)
##	final_label<-paste(substitute(expression(l[i]), list(l = label1,i=l_index)),t_index);
##	final_label<-substitute(expression(paste("Area (", cm^2, ")", sep = "")),list(cm="t"));
##	substitute(group(cm[num],plain(texto)),list(cm="t",num=l_index,texto=t_index))
#	this_type<-substr(type[index],1,2)
#	if(this_type=="c3"){
#		text(happy_count,size+shift_y,levels(l_labels[[index]]),pos=4,col=colors()[color_panel[index]],cex=1.2)
#	} else if(this_type=="c1") {
#		text(happy_count,size+shift_y,levels(l_labels[[index]]),pos=2,col=colors()[color_panel[index]],cex=1.2)
#	} else if(this_type=="c2") {
#		text(happy_count-(shift_x*9.3),size*.95,levels(l_labels[[index]]),pos=1,col=colors()[color_panel[index]],cex=1.2)
#	} else {
#		text(happy_count+shift_x,size+shift_y,levels(l_labels[[index]]),col=colors()[color_panel[index]],cex=1.2)
#	}
#	index<-index+1;
#}
#
#
#axis (2, las=1,at = y )
#
#axis(1, at = 100*seq(0,max_happiness), 
#		labels = 100*seq(0,max_happiness))
#
## Create a legend at (1, max_y) that is slightly smaller 
## (cex) and uses the same line colors and points used by 
## the actual plots
##index<-1;
##labels<-rep("",(length(type)-end_list));
##for (i in type) {
##	info<-levels(l_labels[[i]]);
###	label1<-substr(info,1,1)
###	num<-as.numeric(substr(type[index],start_index,last_index))
##	ahat<-"cm";
##	up<-as.numeric("2");
##	a<-2;
##	labels[index]<-info;
###	labels[index]<-bquote(.(metric)[.(up)]);
###	expression(paste("Area (", .(metric)^2, ")", sep = ""));
###	print(levels(l_labels[[i]]))
###	print(labels[index])
##	if(index==(length(type)-end_list))
##		break
##	index<-index+1;
##}
##text(0.28*max(x), max(y)*.93,"fixed",pos=4)
##legend(0.3*max(x), max(y)*.9,col=colors()[color_panel[1:(length(storage)-end_list)]], 
##		pch=1:(length(type)-end_list),  
##		labels, 
##		cex=0.8);
##
##index<-1;
##cache_labels<-rep(0,each=(end_list));
##cache_index<-1;
##for (i in type) {
##	if(index>(length(type)-end_list)){
##		cache_labels[cache_index]<-levels(l_labels[[i]]);
##		cache_index<-cache_index+1;
##	}
##	index<-index+1;
##}
##
##color_range<-color_panel[(length(storage)-(end_list-1)):(length(storage))]
##points_range<-(length(type)-(end_list-1)):(length(type))
##text(0.48*max(x), max(y)*.93,"cache",pos=4)
##legend(0.5*max(x), max(y)*.9,col=colors()[color_range], 
##		pch=points_range,  
##		cache_labels, 
##		cex=0.8);
#
## Create a title with a red, bold/italic font
##title(main="Fixed number of replicas", 
##		col.main="black", font.main=4)
#
##horizontal lines
##abline( h= y, lty=2)
