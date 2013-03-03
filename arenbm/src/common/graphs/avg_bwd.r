#notes:
#	1) generate input files: 
#sh mining/pick_cache.sh 4800 /media/DisqueC/Temp/april_analysis/aren/general/output_lru_400req_140M_1sec_verbose/experiment_1/1/oracle.log /media/DisqueC/Temp/april_analysis/aren/general/output_dar_400req_140M_1sec_verbose/experiment_1/1/oracle.log /media/DisqueC/Temp/april_analysis/aren/general/output_aren_400req_140M_1sec_verbose/experiment_1/1/oracle.log
#
#	2) run this from a R terminal using source command , source("cache_usage.r"), output in output/cache_usage.png 
#	recommended
#	R --slave --no-save --no-restore --no-environ --silent < avg_bwd.r

convertSecondsToMinutes<-function(vector){
	index<-1;
	for (i in vector) {
		vector[index]<-i/60;
		index<-index+1;
	}
	return(vector);
}



mytable <- read.table("/tmp/avg_bwd.txt",
		header = TRUE,sep=",")


ts<-mytable$ts
#bb<-mytable$e1
#lm<-mytable$e2
pm<-mytable$e1
cm<-mytable$e2

max_values<-c(max(cm),max(pm))
max_y<-floor(max(max_values))+1

#for more colors, 
#see 
#http://research.stowers-institute.org/efg/R/Color/Chart/
plot_colors <- colors()[c(26,507)]

pdf(file="output/avg_bwd.pdf", 
		height=6, width=10, bg = "transparent")
#png(filename="output/cache_usage.png", 
#		height=1600, width=3200, bg = "transparent",res=1200)


#fine control of margens of a figure
#par(mar=c(2,2.8,.5,.5))
#setting maring for small figures
par(mar=c(3.2,3,0.1,.1)) #margins, c(bottom, left, top, right)
par(mgp=c(2,.8,0)) #positions of axis labels and titles, as well, axis lines
global_cex=2
par(cex=global_cex) #text length
#global_cex=.5
#global_lwd=.8
#par(mar=c(2.4,2.5,.4,.4)) #margins, c(bottom, left, top, right)
#par(cex=global_cex) #text length
#par(cex.axis=.9) #axis width in relation to global cex
#par(lwd=global_lwd) #graphic lines
##x-axis and y-axis line width
#x_axis_lwd=global_lwd
#y_axis_lwd=global_lwd
#par(mgp=c(1.2,.6,0)) #positions of axis labels and titles, as well, axis lines
##par(lwd=.1)


#fine control of margens of a figure
#par(mar=c(2,2.8,.5,.5))


# Graph cars using a y axis that ranges from 0 to 12
#plot(cm,  type="l",col=plot_colors[1], ylim=c(0,max(cm)),
#		axes=FALSE, ann = FALSE, yaxt = "n", lty=2)
plot(cm,  type="l",col=plot_colors[1], ylim=c(0,max(cm)),
		axes=FALSE, ann = FALSE, yaxt = "n", lty=2)

mtext(side = 2, text = "Average bandwidth (Mbps)", line = 1.8, cex=global_cex)#, cex=global_cex)
mtext(side = 1, text = "Time (minutes)", line = 2.2, cex=global_cex)#, cex=global_cex)

print(length(cm))
# Make x axis using Mon-Fri labels
	axis(1, at = (0:(length(cm))), 
			labels = 5*((0-1):(length(cm)-1)))#,padj=-.5,lwd=global_lwd)
#axis(2, las=1, at=0.5*0:max_y)
axis(2, las=1, at=0:max_y)#,lwd=global_lwd)#,cex.axis=.5)
#axis(2, las=1, at=1:3,labels = c(10,100,1000),cex.axis=.5)

# Create a legend at (1, max_y) that is slightly smaller 
# (cex) and uses the same line colors and points used by 
# the actual plots
#legend(length(cache)/3, (max_y/10)*3,col=plot_colors, 
#		lty=1:2,
#		c("LRU cache(size=1%)","AREN-TC(10%,30%)"), 
#		bty="n", cex=.4);


# Create box around plot
box()

# Graph trucks with red dashed line and square points
#lines(lm, type="o", lty=3, col=plot_colors[3],pch=24, cex=.8)#,lwd=.5)
lines(pm, type="o", lty=1, col=plot_colors[2],pch=18, cex=.8)#,lwd=.5)
lines(cm, type="o", lty=1, col=plot_colors[1],pch=22, cex=.8)#,lwd=.5)

legend("bottomright",col=plot_colors, 
		text.col=plot_colors, lty=c(1,1),
		c("connection-oriented scheduler","packet-based scheduler"),
		bty="n",pch=c(22,18));

#lines(arentc,  type="l",lty=3, col="green")

# Create a title with a red, bold/italic font
#title(main="Autos", col.main="red", font.main=4)
dev.off()