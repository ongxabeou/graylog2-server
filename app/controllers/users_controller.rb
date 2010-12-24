class UsersController < ApplicationController
  if User.all.size > 0
    filter_resource_access
  end

  if User.find(:all).size == 0
    skip_before_filter :login_required, :only => [:new, :create]
    layout "login"
  end

  def index
    @users = User.find :all
  end

  def show
    @user = User.find(params[:id])
  end

  def new
    @user = User.new
  end

  def edit
    @user = User.find params[:id]
  end

  def create
    @user = User.new(params[:user])
    success = @user && @user.save
    if success && @user.errors.empty?
      redirect_to :action => 'index'
      flash[:notice] = "User has been created."
    else
      flash[:error]  = "Could not create user."
      render :action => 'new'
    end
  end

  def update
    params[:user].delete :password if params[:user][:password].blank?
    params[:user].delete :password_confirmation if params[:user][:password_confirmation].blank?

    @user = User.update params[:id], params[:user]

    if @user.save
      flash[:notice] = 'User has been updated'
      redirect_to :action => 'index'
    else
      flash[:error] = 'Could not update user'
      render :action => 'edit'
    end
  end

  def delete
    # Don't let the user delete the last user.
    if User.count == 1
      flash[:error] = "You cannot delete all users."
      redirect_to :action => 'index'
      return
    end

    user = User.find params[:id]
    if user.destroy
      # Send back to login page if the user deleted himself.
      if current_user.id == params[:id]
        logout_killing_session!
        redirect_to root_path
        return
      end

      flash[:notice] = "User has been deleted."
    else
      flash[:error] = "Could not delete user."
    end

    redirect_to :action => 'index'
  end

end
